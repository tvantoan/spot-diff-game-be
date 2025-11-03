package imggame.network.packets;

public class GuessPointRequest extends BasePacket {
	private static final long serialVersionUID = 1L;

	private String roomId;
	private int userId;
	private int x;
	private int y;

	public GuessPointRequest(String roomId, int userId, int x, int y) {
		this.roomId = roomId;
		this.userId = userId;
		this.x = x;
		this.y = y;
	}

	public String getRoomId() {
		return roomId;
	}

	public int getUserId() {
		return userId;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	@Override
	public String getType() {
		return "GUESS_POINT";
	}
}
