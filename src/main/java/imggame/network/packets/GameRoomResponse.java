package imggame.network.packets;

import imggame.models.User;

public class GameRoomResponse extends BasePacket {
	private static final long serialVersionUID = 1L;

	private String roomId;
	private String state;
	private User player1;
	private User player2;
	private String imageOriginalPath;
	private String imageDiffPath;
	private int totalDifferences;

	public GameRoomResponse(String roomId, String state, User player1, User player2,
			String imageOriginalPath, String imageDiffPath, int totalDifferences) {
		this.roomId = roomId;
		this.state = state;
		this.player1 = player1;
		this.player2 = player2;
		this.imageOriginalPath = imageOriginalPath;
		this.imageDiffPath = imageDiffPath;
		this.totalDifferences = totalDifferences;
	}

	public String getRoomId() {
		return roomId;
	}

	public String getState() {
		return state;
	}

	public User getPlayer1() {
		return player1;
	}

	public User getPlayer2() {
		return player2;
	}

	public String getImageOriginalPath() {
		return imageOriginalPath;
	}

	public String getImageDiffPath() {
		return imageDiffPath;
	}

	public int getTotalDifferences() {
		return totalDifferences;
	}

	@Override
	public String getType() {
		return "GAME_ROOM_RESPONSE";
	}
}
