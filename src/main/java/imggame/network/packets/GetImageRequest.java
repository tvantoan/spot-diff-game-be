package imggame.network.packets;

import imggame.network.types.PacketType;

public class GetImageRequest extends BasePacket {
	private static final long serialVersionUID = 1L;
	public String imagePath;
	public boolean isOriginal;

	public GetImageRequest(String imagePath, boolean isOriginal) {
		this.imagePath = imagePath;
		this.isOriginal = isOriginal;
	}

	@Override
	public PacketType getType() {
		return PacketType.REQUEST;
	}
}
