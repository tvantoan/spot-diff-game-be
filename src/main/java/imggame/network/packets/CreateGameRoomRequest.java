package imggame.network.packets;

import imggame.network.types.PacketType;

public class CreateGameRoomRequest extends BasePacket {
	private static final long serialVersionUID = 1L;

	public int userId;
	public String roomName;

	public CreateGameRoomRequest(int userId) {
		this.userId = userId;
		this.roomName = null;
	}

	public CreateGameRoomRequest(int userId, String roomName) {
		this.userId = userId;
		this.roomName = roomName;
	}

	@Override
	public PacketType getType() {
		return PacketType.REQUEST;
	}
}
