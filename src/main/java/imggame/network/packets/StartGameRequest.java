package imggame.network.packets;

public class StartGameRequest extends BasePacket {
	private static final long serialVersionUID = 1L;

	private String roomId;

	public StartGameRequest(String roomId) {
		this.roomId = roomId;
	}

	public String getRoomId() {
		return roomId;
	}

	@Override
	public String getType() {
		return "START_GAME";
	}
}
