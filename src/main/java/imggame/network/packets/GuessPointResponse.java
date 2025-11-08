package imggame.network.packets;

import imggame.game.DiffBox;
import imggame.network.types.PacketType;

public class GuessPointResponse extends BasePacket {
	private static final long serialVersionUID = 1L;

	public boolean correct;
	public DiffBox foundBox;
	public int score;
	public int remainingDifferences;
	public int guessId;
	public int guessX;
	public int guessY;

	public GuessPointResponse(int guessId, boolean correct, DiffBox foundBox,
			int score, int remainingDifferences, int guessX, int guessY) {
		this.correct = correct;
		this.foundBox = foundBox;
		this.score = score;
		this.remainingDifferences = remainingDifferences;
		this.guessId = guessId;
		this.guessX = guessX;
		this.guessY = guessY;

	}

	@Override
	public PacketType getType() {
		return PacketType.ROOM_RESPONSE;
	}
}
