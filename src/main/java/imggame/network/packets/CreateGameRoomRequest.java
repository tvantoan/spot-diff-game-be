package imggame.network.packets;

public class CreateGameRoomRequest extends BasePacket {
	private static final long serialVersionUID = 1L;

	private int userId;

	public CreateGameRoomRequest(int userId) {
		this.userId = userId;
	}

	public int getUserId() {
		return userId;
	}

	@Override
	public String getType() {
		return "CREATE_GAME_ROOM";
	}
}
