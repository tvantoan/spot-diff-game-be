package imggame.network.packets;

import imggame.network.types.PacketType;

public class StartGameRequest extends BasePacket {
	private static final long serialVersionUID = 1L;

	public String roomId;
	public int userId;

	public StartGameRequest(String roomId, int userId) {
		this.roomId = roomId;
		this.userId = userId;
	}

	@Override
	public PacketType getType() {
		return PacketType.REQUEST;
	}
}
