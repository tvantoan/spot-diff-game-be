package imggame.network.packets;

import imggame.network.types.PacketType;

public class LeaveGameRoomRequest extends BasePacket {
	private static final long serialVersionUID = 1L;

	public int userId;
	public String roomId;

	public LeaveGameRoomRequest(int userId, String roomId) {
		this.userId = userId;
		this.roomId = roomId;
	}

	@Override
	public PacketType getType() {
		return PacketType.REQUEST;
	}
}
