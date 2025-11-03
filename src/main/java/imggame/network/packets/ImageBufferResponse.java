package imggame.network.packets;

public class ImageBufferResponse extends BasePacket {
	private static final long serialVersionUID = 1L;
	public byte[] imageBuffer;

	public ImageBufferResponse(byte[] imageBuffer) {
		this.imageBuffer = imageBuffer;
	}

	public byte[] getImageBuffer() {
		return imageBuffer;
	}

	public void setImageBuffer(byte[] imageBuffer) {
		this.imageBuffer = imageBuffer;
	}

}
