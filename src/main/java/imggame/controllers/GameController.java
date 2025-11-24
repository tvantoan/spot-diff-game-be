package imggame.controllers;

import java.util.List;

import javax.imageio.ImageIO;

import imggame.game.DiffBox;
import imggame.game.GameRoom;
import imggame.game.GameRoomManager;
import imggame.game.ImageSet;
import imggame.game.Player;
import imggame.game.GameRoom.GameState;
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
			GameRoom room = roomManager.createRoom(player, request.roomName);
			this.userRepository.updateInGameStatus(player.info.getId(), false);
			return new GameRoomResponse(
					room.getId(),
					room.getRoomName(),
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
					room.getRoomName(),
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
				System.out.println("Both players are ready. Starting the game...");
				room.setState(GameState.READY);
				room.start();
			} else {
				return new MessagePacket("Waiting for the other player to be ready", "ROOM_WAIT_READY");
			}

			return new GameStartNotification(
					room.getId(),
					room.getCurrentPlayer().info.getId(),
					room.getImageSet().getOriginImagePath(),
					room.getImageSet().getDiffImagePath());

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
				room.endGame();
				if (room.getWinner() == null) {
					return this.handlerGameEnd(room.getPlayer1().info, room.getPlayer2().info, true);
				}
				return this.handlerGameEnd(room.getWinner(), room.getLoser(), false);
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

	public Object updateRoomStates() {
		try {
			List<GameRoom> rooms = roomManager.getAllRooms();
			for (GameRoom room : rooms) {
				if (room.getState() == GameRoom.GameState.PLAYING) {
					GameStateUpdateNotification update = new GameStateUpdateNotification(
							room.getId(),
							room.getState(),
							room.getPlayer1().timer,
							room.getPlayer2().timer,
							room.getPlayer1().score,
							room.getPlayer2().score,
							room.getPointLeftToGuess(),
							room.getCurrentPlayer().info.getId() == room.getPlayer1().info.getId());
					return update;
				}
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return new ErrorResponse("Error when updating room states: " + e.getMessage());
		}
	}

	private Object handlerGameEnd(User winner, User loser, boolean isDraw) {
		int winnerElo = winner != null ? winner.getElo() : 0;
		int loserElo = loser != null ? loser.getElo() : 0;
		if (isDraw) {
			int winnerEloChange = updateUserGameResult(winner, null, loserElo);
			int loserEloChange = updateUserGameResult(loser, null, winnerElo);
			return new GameEndNotification(
					winner != null ? winner.getId() : null,
					loser != null ? loser.getId() : null,
					winnerEloChange,
					loserEloChange);
		}
		int winnerEloChange = updateUserGameResult(winner, true, loserElo);
		int loserEloChange = updateUserGameResult(loser, false, winnerElo);
		return new GameEndNotification(
				winner.getId(),
				loser.getId(),
				winnerEloChange,
				loserEloChange);
	}

	private int updateUserGameResult(User user, Boolean hasWon, int opponentElo) {
		int currentElo = user.getElo();
		int K = 32;
		double actualScore;
		if (hasWon == null) {
			actualScore = 0.5;
		} else {
			actualScore = hasWon ? 1.0 : 0.0;
		}
		double expectedScore = 1.0 / (1.0 + Math.pow(10, (opponentElo - currentElo) / 400.0));
		int eloChange = (int) Math.round(K * (actualScore - expectedScore));
		int newElo = currentElo + eloChange;
		user.setElo(newElo);
		this.userRepository.update(user);
		return eloChange;
	}

	public Object handleGetGameRoomInfo(GetRoomRequest request) {
		try {
			GameRoom room = roomManager.getRoom(request.roomId);
			if (room == null) {
				return new ErrorResponse("Game room not found");
			}
			return new GameRoomResponse(
					room.getId(),
					room.getRoomName(),
					room.getState().toString(),
					room.getPlayer1() != null ? room.getPlayer1().info : null,
					room.getPlayer2() != null ? room.getPlayer2().info : null);
		} catch (Exception e) {
			e.printStackTrace();
			return new ErrorResponse("Error when getting game room info: " + e.getMessage());
		}
	}

	public Object handleLeaveGameRoom(LeaveGameRoomRequest request) {
		try {
			System.out.println("handleLeaveGameRoom called for userId: " + request.userId);
			GameRoom room = roomManager.getRoomByPlayer(request.userId);

			// Kiểm tra room có tồn tại không
			if (room == null) {
				System.out.println("Room not found for userId: " + request.userId);
				return new ErrorResponse("You are not in any room");
			}

			System.out.println("Found room: " + room.getId() + ", state: " + room.getState());

			// Tìm người chơi còn lại
			User remainingPlayer = null;
			Boolean isPlayer1Leaving = null;
			if (room.getPlayer1() != null && room.getPlayer1().info.getId() != request.userId) {
				remainingPlayer = room.getPlayer1().info;
				isPlayer1Leaving = false;
				System.out.println("Remaining player: Player1 - " + remainingPlayer.getUsername());
			} else if (room.getPlayer2() != null && room.getPlayer2().info.getId() != request.userId) {
				remainingPlayer = room.getPlayer2().info;
				isPlayer1Leaving = true;
				System.out.println("Remaining player: Player2 - " + remainingPlayer.getUsername());
			} else {
				System.out.println("No remaining player");
			}

			// Nếu game đang chơi, người rời phòng sẽ thua
			Object response = null;
			if (room.getState() == GameState.PLAYING && remainingPlayer != null) {
				System.out.println("Game is playing, ending game...");
				User leavingPlayer = room.getPlayerById(request.userId).info;
				response = this.handlerGameEnd(remainingPlayer, leavingPlayer, false);
				System.out.println("Game ended, response created");
			}

			// KHÔNG xóa player ngay - chỉ dừng game và cập nhật status
			// Việc xóa player sẽ được thực hiện trong GameServer sau khi gửi response
			System.out.println("Stopping game state (not removing player yet)...");
			if (room.getState() == GameState.PLAYING) {
				room.setState(GameState.FINISHED);
			}

			this.userRepository.updateInGameStatus(request.userId, false);
			System.out.println("Updated in-game status to false");

			// Trả về kết quả
			if (response != null) {
				System.out.println("Returning game end response");
				return response;
			} else {
				// Nếu không phải đang chơi, trả về thông tin phòng
				if (remainingPlayer != null) {
					System.out.println("Returning room response");
					return new GameRoomResponse(
							room.getId(),
							room.getRoomName(),
							room.getState().toString(),
							isPlayer1Leaving ? null : remainingPlayer,
							isPlayer1Leaving ? remainingPlayer : null);
				} else {
					// Phòng trống, trả về message
					System.out.println("Returning message packet");
					return new MessagePacket("Left the game room successfully", MessageContext.PLAYER_LEFT_ROOM);
				}
			}

		} catch (Exception e) {
			System.err.println("Error in handleLeaveGameRoom: " + e.getMessage());
			e.printStackTrace();
			return new ErrorResponse("Error when leaving room: " + e.getMessage());
		}
	}

	// Method mới để xóa player sau khi đã gửi response
	public void finalizePlayerLeave(int userId) {
		try {
			System.out.println("Finalizing player leave for userId: " + userId);
			roomManager.playerLeaveRoom(userId);
			System.out.println("Player removed from room successfully");
		} catch (Exception e) {
			System.err.println("Error finalizing player leave: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public Object handlePlayerDisconnect(int userId) {
		try {
			GameRoom room = roomManager.getRoomByPlayer(userId);
			this.userRepository.updateInGameStatus(userId, false);
			this.userRepository.updateOnlineStatus(userId, false);
			if (room != null) {
				Object response = handleLeaveGameRoom(new LeaveGameRoomRequest(userId, room.getId()));
				return response;
			}
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

			String diffImagePath = "/dataset/" + selectedImageSet.getString("diff_image");
			String orgImagePath = "/dataset/" + selectedImageSet.getString("org_image");

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
			String resourcePath;
			if (!request.imagePath.startsWith("/")) {
				resourcePath = "/" + request.imagePath;
			} else {
				resourcePath = request.imagePath;
			}
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

			return new ImageBufferResponse(imageBytes, request.isOriginal);
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

	public Object handleGetRoomList() {
		try {
			List<GameRoom> allRooms = this.roomManager.getAllRooms();
			return new RoomListResponse(allRooms);
		} catch (Exception e) {
			e.printStackTrace();
			return new ErrorResponse("Error when getting room list: " + e.getMessage());
		}
	}
}
