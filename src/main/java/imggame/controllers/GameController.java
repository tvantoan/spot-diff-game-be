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
import imggame.repository.UserRepository;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;

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

	public Object handleCreateGameRoom(CreateGameRoomRequest request, Player player) {
		try {
			if (roomManager.isPlayerInRoom(request.getUserId())) {
				return new ErrorResponse("Already in a game room");
			}

			ImageSet imageSet = getRandomImageSet();

			GameRoom room = roomManager.createRoom(player, imageSet);
			this.userRepository.updateInGameStatus(player.info.getId(), false);
			return new GameRoomResponse(
					room.getId(),
					room.getState().toString(),
					player.info,
					null,
					imageSet.getOriginImagePath(),
					imageSet.getDiffImagePath(),
					imageSet.getTotalDifferences());

		} catch (Exception e) {
			e.printStackTrace();
			return new ErrorResponse("Error when create ew room " + e.getMessage());
		}
	}

	public Object handleJoinGameRoom(JoinGameRoomRequest request, Player player) {
		try {
			if (roomManager.isPlayerInRoom(request.getUserId())) {
				return new ErrorResponse("You are already in a game room");
			}

			GameRoom room = roomManager.joinRoom(request.getRoomId(), player);

			if (room == null) {
				return new ErrorResponse("Cannot join the game room");
			}
			this.userRepository.updateInGameStatus(player.info.getId(), true);

			return new GameRoomResponse(
					room.getId(),
					room.getState().toString(),
					room.getPlayer1().info,
					room.getPlayer2().info,
					room.getImageSet().getOriginImagePath(),
					room.getImageSet().getDiffImagePath(),
					room.getImageSet().getTotalDifferences());

		} catch (Exception e) {
			e.printStackTrace();
			return new ErrorResponse("Error when joining the room: " + e.getMessage());
		}
	}

	public Object handleStartGame(StartGameRequest request) {
		try {
			GameRoom room = roomManager.getRoom(request.getRoomId());

			if (room == null) {
				return new ErrorResponse("Unable to find the game room");
			}

			if (!room.isFull()) {
				return new ErrorResponse("Not enough players");
			}

			room.start();

			return new GameStartNotification(
					room.getId(),
					room.getPlayer1().info,
					room.getPlayer2().info,
					room.getCurrentPlayer().info.getId(),
					AppConfig.MAX_TIME_PER_ROUND_SECONDS);

		} catch (Exception e) {
			e.printStackTrace();
			return new ErrorResponse("Error when starting game: " + e.getMessage());
		}
	}

	public Object handleGuessPoint(GuessPointRequest request) {
		try {
			GameRoom room = roomManager.getRoom(request.getRoomId());

			if (room == null) {
				return new ErrorResponse("Unable to find the game room");
			}

			if (room.getState() != GameRoom.GameState.PLAYING) {
				return new ErrorResponse("Game has not started or has already ended");
			}

			Player currentPlayer = room.getCurrentPlayer();
			if (currentPlayer.info.getId() != request.getUserId()) {
				return new ErrorResponse("Not your turn");
			}

			DiffBox foundBox = room.guessPoint(request.getUserId(), request.getX(), request.getY());

			return new GuessPointResponse(
					request.getUserId(),
					foundBox != null,
					foundBox,
					currentPlayer.score,
					room.getPointLeftToGuess(),
					request.getX(),
					request.getY());

		} catch (Exception e) {
			e.printStackTrace();
			return new ErrorResponse("Error when processing guess point: " + e.getMessage());
		}
	}

	public Object handleLeaveGameRoom(LeaveGameRoomRequest request) {
		try {
			roomManager.playerLeaveRoom(request.getUserId());
			this.userRepository.updateInGameStatus(request.getUserId(), false);
			return new BasePacket();
		} catch (Exception e) {
			e.printStackTrace();
			return new ErrorResponse("Error when leaving room: " + e.getMessage());
		}
	}

	public List<GameRoom> getWaitingRooms() {
		return roomManager.getWaitingRooms();
	}

	public ImageSet getRandomImageSet() {
		// TODO: Implement việc lấy random từ database hoặc folder
		// Hiện tại return default image set
		return new ImageSet();
	}

	public Object getImageResource(GetImageRequest request) {
		try {

			BufferedImage image = ImageIO.read(new File(request.imagePath));
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(image, "jpg", baos);
			byte[] imageBytes = baos.toByteArray();
			return new ImageBufferResponse(imageBytes);
		} catch (Exception e) {
			e.printStackTrace();
			return new ErrorResponse("Error get image resourse" + e.getMessage());
		}
	}

	public Object handleInviteRequest(InviteRequest request) {
		try {
			User inviteUser = this.userRepository.findById(request.senderId);
			if (inviteUser == null) {
				return new ErrorResponse("User not found");
			}
			this.userRepository.updateInGameStatus(request.senderId, true);
			GameRoom room = this.roomManager.createRoom(new Player(inviteUser), getRandomImageSet());
			InviteResponse response = new InviteResponse(inviteUser, room.getId());
			return response;
		} catch (Exception e) {
			e.printStackTrace();
			return new ErrorResponse("Error when inviting player: " + e.getMessage());
		}
	}
}
