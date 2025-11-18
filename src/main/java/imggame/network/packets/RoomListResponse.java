package imggame.network.packets;

import java.util.List;

import imggame.game.GameRoom;
import imggame.network.types.PacketType;

class RoomResponse {
	public String roomId;
	public String roomName;
	public int playerCount;
	public int maxPlayers;

	public RoomResponse(String roomId, String roomName, int playerCount, int maxPlayers) {
		this.roomId = roomId;
		this.roomName = roomName;
		this.playerCount = playerCount;
		this.maxPlayers = maxPlayers;
	}
}

public class RoomListResponse extends BasePacket {
	private List<RoomResponse> rooms;

	public RoomListResponse(List<GameRoom> rooms) {
		this.rooms = rooms.stream()
				.map(room -> new RoomResponse(
						room.getId(),
						room.getRoomName(),
						room.getTotalPlayers(),
						2))
				.toList();

	}

	public List<RoomResponse> getRooms() {
		return rooms;
	}

	@Override
	public PacketType getType() {
		return PacketType.DIRECT_RESPONSE;
	}
}
