package imggame.network.packets;

import imggame.network.types.PacketType;

public class MessagePacket extends BasePacket {
	public String message;
	public String context;
	public PacketType packetType = PacketType.DIRECT_RESPONSE;

	public MessagePacket(String message) {
		this.message = message;
		this.context = null;
	}

	public MessagePacket(String message, String context) {
		this.message = message;
		this.context = context;
	}

	@Override
	public PacketType getType() {
		return packetType;
	}
}
