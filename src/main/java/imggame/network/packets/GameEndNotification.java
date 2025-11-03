package imggame.network.packets;

import imggame.models.User;

public class GameEndNotification extends BasePacket {
	private static final long serialVersionUID = 1L;

	private String roomId;
	private User winner;
	private User loser;
	private int player1Score;
	private int player2Score;
	private long gameDurationSeconds;

	public GameEndNotification(String roomId, User winner, User loser,
			int player1Score, int player2Score, long gameDurationSeconds) {
		this.roomId = roomId;
		this.winner = winner;
		this.loser = loser;
		this.player1Score = player1Score;
		this.player2Score = player2Score;
		this.gameDurationSeconds = gameDurationSeconds;
	}

	public String getRoomId() {
		return roomId;
	}

	public User getWinner() {
		return winner;
	}

	public User getLoser() {
		return loser;
	}

	public int getPlayer1Score() {
		return player1Score;
	}

	public int getPlayer2Score() {
		return player2Score;
	}

	public long getGameDurationSeconds() {
		return gameDurationSeconds;
	}

	@Override
	public String getType() {
		return "GAME_END";
	}
}
