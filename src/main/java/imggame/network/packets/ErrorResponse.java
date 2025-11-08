package imggame.network.packets;

import imggame.network.types.PacketType;

public class ErrorResponse extends BasePacket {
	public String message;

	public ErrorResponse(String message) {
		this.message = message;
	}

	@Override
	public PacketType getType() {
		return PacketType.DIRECT_RESPONSE;
	}
}
