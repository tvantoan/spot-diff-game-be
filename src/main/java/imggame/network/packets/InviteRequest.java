package imggame.network.packets;

import imggame.network.types.PacketType;

public class InviteRequest extends BasePacket {
	public int senderId;
	public int receiverId;

	public InviteRequest(int senderId, int receiverId) {
		this.senderId = senderId;
		this.receiverId = receiverId;
	}

	@Override
	public PacketType getType() {
		return PacketType.REQUEST;
	}
}
