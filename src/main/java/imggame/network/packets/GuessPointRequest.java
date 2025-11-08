package imggame.network.packets;

import imggame.network.types.PacketType;

public class GuessPointRequest extends BasePacket {
	private static final long serialVersionUID = 1L;

	public String roomId;
	public int userId;
	public int x;
	public int y;

	public GuessPointRequest(String roomId, int userId, int x, int y) {
		this.roomId = roomId;
		this.userId = userId;
		this.x = x;
		this.y = y;
	}

	@Override
	public PacketType getType() {
		return PacketType.REQUEST;
	}
}
