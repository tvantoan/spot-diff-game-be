package imggame.network.packets;

import imggame.network.types.PacketType;

public class ImageBufferResponse extends BasePacket {
	private static final long serialVersionUID = 1L;
	public byte[] imageBuffer;
	public boolean isOriginal;

	public ImageBufferResponse(byte[] imageBuffer, boolean isOriginal) {
		this.imageBuffer = imageBuffer;
		this.isOriginal = isOriginal;
	}

	@Override
	public PacketType getType() {
		return PacketType.DIRECT_RESPONSE;
	}
}
