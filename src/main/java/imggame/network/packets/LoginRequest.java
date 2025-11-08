package imggame.network.packets;

import imggame.network.types.PacketType;

public class LoginRequest extends BasePacket {
	public String username;
	public String password;

	public LoginRequest(String username, String password) {
		this.username = username;
		this.password = password;
	}

	@Override
	public PacketType getType() {
		return PacketType.REQUEST;
	}
}
