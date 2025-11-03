package imggame.network.packets;

public class GameStateUpdateNotification extends BasePacket {
	private static final long serialVersionUID = 1L;

	private String roomId;
	private int currentTurnUserId;
	private int timerSeconds;
	private int player1Score;
	private int player2Score;

	public GameStateUpdateNotification(String roomId, int currentTurnUserId,
			int timerSeconds, int player1Score, int player2Score) {
		this.roomId = roomId;
		this.currentTurnUserId = currentTurnUserId;
		this.timerSeconds = timerSeconds;
		this.player1Score = player1Score;
		this.player2Score = player2Score;
	}

	public String getRoomId() {
		return roomId;
	}

	public int getCurrentTurnUserId() {
		return currentTurnUserId;
	}

	public int getTimerSeconds() {
		return timerSeconds;
	}

	public int getPlayer1Score() {
		return player1Score;
	}

	public int getPlayer2Score() {
		return player2Score;
	}

	@Override
	public String getType() {
		return "GAME_STATE_UPDATE";
	}
}
