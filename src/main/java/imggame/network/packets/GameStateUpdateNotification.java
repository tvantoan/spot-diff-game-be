package imggame.network.packets;

import imggame.game.GameRoom.GameState;
import imggame.network.types.PacketType;

public class GameStateUpdateNotification extends BasePacket {
	private static final long serialVersionUID = 1L;

	public String roomId;
	public GameState gameState;
	public int player1Timer;
	public int player2Timer;
	public int player1Score;
	public int player2Score;
	public int pointLeftToGuess;
	public boolean isPlayer1Turn;

	public GameStateUpdateNotification(String roomId, GameState gameState, int player1Timer, int player2Timer,
			int player1Score, int player2Score, int pointLeftToGuess, boolean isPlayer1Turn) {
		this.roomId = roomId;
		this.gameState = gameState;
		this.player1Timer = player1Timer;
		this.player2Timer = player2Timer;
		this.player1Score = player1Score;
		this.player2Score = player2Score;
		this.pointLeftToGuess = pointLeftToGuess;
		this.isPlayer1Turn = isPlayer1Turn;
	}

	@Override
	public PacketType getType() {
		return PacketType.ROOM_RESPONSE;
	}
}
