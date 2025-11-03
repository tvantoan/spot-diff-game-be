package imggame.network.packets;

import imggame.models.User;

public class GameStartNotification extends BasePacket {
	private static final long serialVersionUID = 1L;

	private String roomId;
	private User player1;
	private User player2;
	private int currentTurnUserId;
	private int timerSeconds;

	public GameStartNotification(String roomId, User player1, User player2,
			int currentTurnUserId, int timerSeconds) {
		this.roomId = roomId;
		this.player1 = player1;
		this.player2 = player2;
		this.currentTurnUserId = currentTurnUserId;
		this.timerSeconds = timerSeconds;
	}

	// Getters
	public String getRoomId() {
		return roomId;
	}

	public User getPlayer1() {
		return player1;
	}

	public User getPlayer2() {
		return player2;
	}

	public int getCurrentTurnUserId() {
		return currentTurnUserId;
	}

	public int getTimerSeconds() {
		return timerSeconds;
	}

	@Override
	public String getType() {
		return "GAME_START";
	}
}
