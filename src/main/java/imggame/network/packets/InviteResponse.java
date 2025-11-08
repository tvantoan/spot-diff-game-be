package imggame.network.packets;

import imggame.models.User;
import imggame.network.types.PacketType;

public class InviteResponse extends BasePacket {
	private static final long serialVersionUID = 1L;
	public User inviteUser;
	public String roomId;

	public InviteResponse(User inviteUser, String roomId) {
		this.inviteUser = inviteUser;
		this.roomId = roomId;
	}

	@Override
	public PacketType getType() {
		return PacketType.DIRECT_RESPONSE;
	}
}
