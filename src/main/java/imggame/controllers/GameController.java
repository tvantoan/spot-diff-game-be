package imggame.controllers;

import java.sql.Connection;
import java.util.List;

import javax.imageio.ImageIO;

import imggame.config.AppConfig;
import imggame.database.Database;
import imggame.game.DiffBox;
import imggame.game.GameRoom;
import imggame.game.GameRoomManager;
import imggame.game.ImageSet;
import imggame.game.Player;
import imggame.models.User;
import imggame.network.packets.*;
import imggame.network.types.MessageContext;
import imggame.network.types.PacketType;
import imggame.repository.UserRepository;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import org.json.JSONArray;
import org.json.JSONObject;

public class GameController {
	private UserRepository userRepository;
	private GameRoomManager roomManager;

	public GameController() {
		this.userRepository = new UserRepository();
		this.roomManager = new GameRoomManager();
	}

	public GameRoomManager getRoomManager() {
		return this.roomManager;
	}

	public Object handleCreateGameRoom(CreateGameRoomRequest request) {
		try {

			User user = this.userRepository.findById(request.userId);
			if (user == null) {
				return new ErrorResponse("User not found");
			}

			if (roomManager.isPlayerInRoom(request.userId)) {
				return new ErrorResponse("Already in a game room");
			}
			Player player = new Player(user);
			GameRoom room = roomManager.createRoom(player);
			this.userRepository.updateInGameStatus(player.info.getId(), false);
			return new GameRoomResponse(
					room.getId(),
					room.getState().toString(),
					player.info,
					null);
		} catch (Exception e) {
			e.printStackTrace();
			return new ErrorResponse("Error when create ew room " + e.getMessage());
		}
	}

	public Object handleJoinGameRoom(JoinGameRoomRequest request) {
		try {
			if (roomManager.isPlayerInRoom(request.userId)) {
				return new ErrorResponse("You are already in a game room");
			}

			User user = this.userRepository.findById(request.userId);
			if (user == null) {
				return new ErrorResponse("User not found");
			}

			Player player = new Player(user);

			GameRoom room = roomManager.joinRoom(request.roomId, player);

			if (room == null) {
				return new ErrorResponse("Cannot join the game room");
			}
			this.userRepository.updateInGameStatus(player.info.getId(), true);

			return new GameRoomResponse(
					room.getId(),
					room.getState().toString(),
					room.getPlayer1().info,
					room.getPlayer2().info);

		} catch (Exception e) {
			e.printStackTrace();
			return new ErrorResponse("Error when joining the room: " + e.getMessage());
		}
	}

	public Object handleStartGame(StartGameRequest request) {
		try {
			GameRoom room = roomManager.getRoom(request.roomId);

			if (room == null) {
				return new ErrorResponse("Unable to find the game room");
			}

			if (!room.isFull()) {
				return new ErrorResponse("Not enough players");
			}

			room.getPlayerById(request.userId).isReady = true;

			if (room.isAllReady()) {
				room.start();
			} else {
				return new MessagePacket("Waiting for the other player to be ready", "ROOM_WAIT_READY");
			}

			return new GameStartNotification(
					room.getId(),
					room.getCurrentPlayer().info.getId(),
					getRandomImageSet().getOriginImagePath(),
					getRandomImageSet().getDiffImagePath());

		} catch (Exception e) {
			e.printStackTrace();
			return new ErrorResponse("Error when starting game: " + e.getMessage());
		}
	}

	public Object handleGuessPoint(GuessPointRequest request) {
		try {
			GameRoom room = roomManager.getRoom(request.roomId);

			if (room == null) {
				return new ErrorResponse("Unable to find the game room");
			}

			if (room.getState() != GameRoom.GameState.PLAYING) {
				return new ErrorResponse("Game has not started or has already ended");
			}

			Player currentPlayer = room.getCurrentPlayer();
			if (currentPlayer.info.getId() != request.userId) {
				return new ErrorResponse("Not your turn");
			}

			DiffBox foundBox = room.guessPoint(request.userId, request.x, request.y);

			if (foundBox != null && room.isGameOver()) {
				room.setState(GameRoom.GameState.WAITING);
				return this.handlerGameEnd(room.getWinner(), room.getLoser());
			}

			return new GuessPointResponse(
					request.userId,
					foundBox != null,
					foundBox,
					currentPlayer.score,
					room.getPointLeftToGuess(),
					request.x,
					request.y);

		} catch (Exception e) {
			e.printStackTrace();
			return new ErrorResponse("Error when processing guess point: " + e.getMessage());
		}
	}

	private Object handlerGameEnd(User winner, User loser) {
		int winnerElo = winner != null ? winner.getElo() : 0;
		int loserElo = loser != null ? loser.getElo() : 0;
		int winnerEloChange = updateUserGameResult(winner, true, loserElo);
		int loserEloChange = updateUserGameResult(loser, false, winnerElo);
		return new GameEndNotification(
				winner.getId(),
				loser.getId(),
				winnerEloChange,
				loserEloChange);
	}

	private int updateUserGameResult(User user, boolean hasWon, int opponentElo) {
		int currentElo = user.getElo();
		int K = 32;
		double expectedScore = 1.0 / (1.0 + Math.pow(10, (opponentElo - currentElo) / 400.0));
		double actualScore = hasWon ? 1.0 : 0.0;
		int eloChange = (int) Math.round(K * (actualScore - expectedScore));
		int newElo = currentElo + eloChange;
		user.setElo(newElo);
		this.userRepository.update(user);
		return eloChange;
	}

	public Object handleLeaveGameRoom(LeaveGameRoomRequest request) {
		try {
			roomManager.playerLeaveRoom(request.userId);
			this.userRepository.updateInGameStatus(request.userId, false);
			MessagePacket messagePacket = new MessagePacket("Left the game room successfully",
					MessageContext.PLAYER_LEFT_ROOM);
			messagePacket.packetType = PacketType.ROOM_RESPONSE;
			return messagePacket;
		} catch (Exception e) {
			e.printStackTrace();
			return new ErrorResponse("Error when leaving room: " + e.getMessage());
		}
	}

	public Object handlePlayerDisconnect(int userId) {
		try {
			roomManager.playerLeaveRoom(userId);
			this.userRepository.updateInGameStatus(userId, false);
			this.userRepository.updateOnlineStatus(userId, false);
			MessagePacket messagePacket = new MessagePacket("Left the game room successfully",
					MessageContext.PLAYER_LEFT_ROOM);
			messagePacket.packetType = PacketType.ROOM_RESPONSE;
			return messagePacket;
		} catch (Exception e) {
			e.printStackTrace();
			return new ErrorResponse("Error when handling player disconnect: " + e.getMessage());
		}
	}

	public List<GameRoom> getWaitingRooms() {
		return roomManager.getWaitingRooms();
	}

	public ImageSet getRandomImageSet() {
		try {
			InputStream inputStream = getClass().getResourceAsStream("/dataset/image_sets.json");
			if (inputStream == null) {
				System.err.println("Cannot find image_sets.json in resources");
				return new ImageSet();
			}

			String jsonContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
			JSONArray imageSetsArray = new JSONArray(jsonContent);

			if (imageSetsArray.isEmpty()) {
				System.err.println("image_sets.json is empty");
				return new ImageSet();
			}

			Random random = new Random();
			int randomIndex = random.nextInt(imageSetsArray.length());
			JSONObject selectedImageSet = imageSetsArray.getJSONObject(randomIndex);

			String diffImagePath = "dataset/" + selectedImageSet.getString("diff_image");
			String orgImagePath = "dataset/" + selectedImageSet.getString("org_image");

			JSONArray diffBoxesArray = selectedImageSet.getJSONArray("diff_boxes");
			DiffBox[] diffBoxes = new DiffBox[diffBoxesArray.length()];

			for (int i = 0; i < diffBoxesArray.length(); i++) {
				JSONObject boxJson = diffBoxesArray.getJSONObject(i);
				diffBoxes[i] = new DiffBox(
						boxJson.getInt("x"),
						boxJson.getInt("y"),
						boxJson.getInt("width"),
						boxJson.getInt("height"));
			}

			int difficulty = Math.min(5, Math.max(1, diffBoxes.length / 2));

			System.out.println("Loaded image set: " + diffImagePath + " with " + diffBoxes.length + " differences");
			return new ImageSet(orgImagePath, diffImagePath, diffBoxes, difficulty);

		} catch (Exception e) {
			System.err.println("Error loading random image set: " + e.getMessage());
			e.printStackTrace();
			return new ImageSet();
		}
	}

	public Object getImageResource(GetImageRequest request) {
		try {
			String resourcePath = "/dataset/" + request.imagePath;
			InputStream inputStream = getClass().getResourceAsStream(resourcePath);

			if (inputStream == null) {
				System.err.println("Cannot find image: " + resourcePath);
				return new ErrorResponse("Image not found: " + request.imagePath);
			}

			BufferedImage image = ImageIO.read(inputStream);
			if (image == null) {
				return new ErrorResponse("Failed to read image: " + request.imagePath);
			}

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(image, "png", baos);
			byte[] imageBytes = baos.toByteArray();

			inputStream.close();

			return new ImageBufferResponse(imageBytes);
		} catch (Exception e) {
			e.printStackTrace();
			return new ErrorResponse("Error getting image resource: " + e.getMessage());
		}
	}

	public Object handleInviteRequest(InviteRequest request) {
		try {
			User inviteUser = this.userRepository.findById(request.senderId);
			if (inviteUser == null) {
				return new ErrorResponse("User not found");
			}
			this.userRepository.updateInGameStatus(request.senderId, true);
			GameRoom room = this.roomManager.createRoom(new Player(inviteUser));
			InviteResponse response = new InviteResponse(inviteUser, room.getId());
			return response;
		} catch (Exception e) {
			e.printStackTrace();
			return new ErrorResponse("Error when inviting player: " + e.getMessage());
		}
	}
}
