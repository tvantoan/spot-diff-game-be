package imggame.network.packets;

import java.util.List;

import imggame.game.GameRoom;
import imggame.network.types.PacketType;

public class RoomListResponse extends BasePacket {
	private static final long serialVersionUID = 1L;
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
