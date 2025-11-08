package imggame.network.packets;

import imggame.models.User;
import imggame.network.types.PacketType;

public class GameEndNotification extends BasePacket {
	private static final long serialVersionUID = 1L;

	public int winnerId;
	public int loserId;
	public int winnerEloChange;
	public int loserEloChange;

	public GameEndNotification(int winnerId, int loserId, int winnerEloChange, int loserEloChange) {
		this.winnerId = winnerId;
		this.loserId = loserId;
		this.winnerEloChange = winnerEloChange;
		this.loserEloChange = loserEloChange;
	}

	@Override
	public PacketType getType() {
		return PacketType.ROOM_RESPONSE;
	}
}
