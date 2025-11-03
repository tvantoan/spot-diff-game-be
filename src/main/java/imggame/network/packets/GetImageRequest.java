package imggame.network.packets;

public class GetImageRequest extends BasePacket {
	private static final long serialVersionUID = 1L;
	public String imagePath;

	public GetImageRequest(String imagePath) {
		this.imagePath = imagePath;
	}
}
