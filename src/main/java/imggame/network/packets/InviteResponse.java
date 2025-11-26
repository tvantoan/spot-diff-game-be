package imggame.network.packets;

import imggame.models.User;
import imggame.network.types.PacketType;

public class InviteResponse extends BasePacket {
	private static final long serialVersionUID = 1L;
	public User inviteUser;
	public String roomId;
	public int receiverId;

	public InviteResponse(int receiverId, User inviteUser, String roomId) {
		this.inviteUser = inviteUser;
		this.receiverId = receiverId;
		this.roomId = roomId;
	}

	@Override
	public PacketType getType() {
		return PacketType.DIRECT_RESPONSE;
	}
}
