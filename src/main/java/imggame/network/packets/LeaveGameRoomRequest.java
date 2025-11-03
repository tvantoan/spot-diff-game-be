package imggame.network.packets;

public class LeaveGameRoomRequest extends BasePacket {
	private static final long serialVersionUID = 1L;

	private int userId;
	private String roomId;

	public LeaveGameRoomRequest(int userId, String roomId) {
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
		return "LEAVE_GAME_ROOM";
	}
}
