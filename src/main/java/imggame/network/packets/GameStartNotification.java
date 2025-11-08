package imggame.network.packets;

import imggame.network.types.PacketType;

public class GameStartNotification extends BasePacket {
	private static final long serialVersionUID = 1L;

	public String roomId;
	public int currentTurnUserId;
	public String originImagePath;
	public String diffImagePath;

	public GameStartNotification(String roomId, int currentTurnUserId, String originImagePath, String diffImagePath) {
		this.roomId = roomId;
		this.currentTurnUserId = currentTurnUserId;
		this.originImagePath = originImagePath;
		this.diffImagePath = diffImagePath;
	}

	@Override
	public PacketType getType() {
		return PacketType.ROOM_RESPONSE;
	}
}
