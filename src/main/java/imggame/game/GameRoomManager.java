package imggame.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GameRoomManager {
	private Map<String, GameRoom> gameRooms = new ConcurrentHashMap<>();
	private Map<Integer, String> playerToRoom = new ConcurrentHashMap<>(); // userId -> roomId

	public GameRoom createRoom(Player player, ImageSet imageSet) {
		GameRoom room = new GameRoom(player, imageSet);
		gameRooms.put(room.getId(), room);
		playerToRoom.put(player.info.getId(), room.getId());
		return room;
	}

	public GameRoom joinRoom(String roomId, Player player) {
		GameRoom room = gameRooms.get(roomId);
		if (room != null && room.addPlayer2(player)) {
			playerToRoom.put(player.info.getId(), roomId);
			return room;
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
				if (room.getState() != GameRoom.GameState.FINISHED) {
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
