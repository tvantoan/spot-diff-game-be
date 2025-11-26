package imggame.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import imggame.network.packets.ErrorResponse;

public class GameRoomManager {
	private Map<String, GameRoom> gameRooms = new ConcurrentHashMap<>();
	private Map<Integer, String> playerToRoom = new ConcurrentHashMap<>(); // userId -> roomId

	public GameRoom createRoom(Player player) {
		GameRoom room = new GameRoom(player);
		gameRooms.put(room.getId(), room);
		playerToRoom.put(player.info.getId(), room.getId());
		return room;
	}

	public GameRoom createRoom(Player player, String roomName) {
		GameRoom room = new GameRoom(player, roomName);
		gameRooms.put(room.getId(), room);
		playerToRoom.put(player.info.getId(), room.getId());
		return room;
	}

	public Object joinRoom(String roomId, Player player) {
		GameRoom room = gameRooms.get(roomId);
		if (room == null) {
			return new ErrorResponse("Game room not found");
		}
		if (room.isEmpty()) {
			return new ErrorResponse("Room is empty!");

		}
		if (room.isFull()) {
			return new ErrorResponse("Room is full!");
		}
		if (room != null && room.addPlayer(player)) {
			playerToRoom.put(player.info.getId(), roomId);
			return (GameRoom) room;
		}
		return null;
	}

	public GameRoom getRoom(String roomId) {
		return gameRooms.get(roomId);
	}

	public GameRoom getRoomByPlayer(int userId) {
		String roomId = playerToRoom.get(userId);
		if (roomId != null) {
			return gameRooms.get(roomId);
		}
		return null;
	}

	public void removeRoom(String roomId) {
		GameRoom room = gameRooms.remove(roomId);
		if (room != null) {
			// Cleanup room trước khi xóa
			room.cleanup();

			if (room.getPlayer1() != null) {
				playerToRoom.remove(room.getPlayer1().info.getId());
			}

			if (room.getPlayer2() != null) {
				playerToRoom.remove(room.getPlayer2().info.getId());
			}
		}
	}

	public void playerLeaveRoom(int userId) {
		String roomId = playerToRoom.remove(userId);
		if (roomId != null) {
			GameRoom room = gameRooms.get(roomId);
			if (room != null) {
				room.playerLeave(userId);
				if (room.isEmpty()) {
					removeRoom(roomId);
				}
			}
		}
	}

	public List<GameRoom> getWaitingRooms() {
		return gameRooms.values().stream()
				.filter(room -> room.getState() == GameRoom.GameState.WAITING)
				.collect(Collectors.toList());
	}

	public GameRoom findAvailableRoom() {
		return gameRooms.values().stream()
				.filter(room -> room.getState() == GameRoom.GameState.WAITING)
				.findFirst()
				.orElse(null);
	}

	public boolean isPlayerInRoom(int userId) {
		return playerToRoom.containsKey(userId);
	}

	public List<GameRoom> getAllRooms() {
		return new ArrayList<>(gameRooms.values());
	}
}
