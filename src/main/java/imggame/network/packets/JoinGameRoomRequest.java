package imggame.network.packets;

public class JoinGameRoomRequest extends BasePacket {
	private static final long serialVersionUID = 1L;

	private int userId;
	private String roomId;

	public JoinGameRoomRequest(int userId, String roomId) {
		this.userId = userId;
		this.roomId = roomId;
	}

	public int getUserId() {
		return userId;
	}

	public String getRoomId() {
		return roomId;
	}

	@Override
	public String getType() {
		return "JOIN_GAME_ROOM";
	}
}
