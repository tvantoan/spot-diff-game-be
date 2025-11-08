package imggame.network.packets;

import imggame.network.types.PacketType;

public class ImageBufferResponse extends BasePacket {
	private static final long serialVersionUID = 1L;
	public byte[] imageBuffer;

	public ImageBufferResponse(byte[] imageBuffer) {
		this.imageBuffer = imageBuffer;
	}

	@Override
	public PacketType getType() {
		return PacketType.DIRECT_RESPONSE;
	}
}
