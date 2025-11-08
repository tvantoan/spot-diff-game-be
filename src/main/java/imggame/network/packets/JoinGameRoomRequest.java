package imggame.network.packets;

import imggame.network.types.PacketType;

public class JoinGameRoomRequest extends BasePacket {
	private static final long serialVersionUID = 1L;

	public int userId;
	public String roomId;

	public JoinGameRoomRequest(int userId, String roomId) {
		this.userId = userId;
		this.roomId = roomId;
	}

	@Override
	public PacketType getType() {
		return PacketType.REQUEST;
	}
}
