package imggame.network.packets;

import imggame.models.User;
import imggame.network.types.PacketType;

public class GameRoomResponse extends BasePacket {
	private static final long serialVersionUID = 1L;

	public String roomId;
	public String state;
	public User player1;
	public User player2;

	public GameRoomResponse(String roomId, String state, User player1, User player2) {
		this.roomId = roomId;
		this.state = state;
		this.player1 = player1;
		this.player2 = player2;
	}

	@Override
	public PacketType getType() {
		return PacketType.DIRECT_RESPONSE;
	}
}
