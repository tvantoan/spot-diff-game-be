package imggame.network.packets;

import imggame.network.types.PacketType;

public class GetImageRequest extends BasePacket {
	private static final long serialVersionUID = 1L;
	public String imagePath;

	public GetImageRequest(String imagePath) {
		this.imagePath = imagePath;
	}

	@Override
	public PacketType getType() {
		return PacketType.REQUEST;
	}
}
