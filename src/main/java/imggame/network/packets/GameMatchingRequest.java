package imggame.network.packets;

import imggame.network.types.PacketType;

public class GameMatchingRequest extends BasePacket {
	public int playerId;

	public GameMatchingRequest(int playerId) {
		this.playerId = playerId;
	}

	@Override
	public PacketType getType() {
		return PacketType.REQUEST;
	}
}
