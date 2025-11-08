package imggame.network.packets;

import imggame.network.types.PacketType;

public class GetPlayerListRequest extends BasePacket {
	public int pageSize;
	public int offset;
	public Boolean isDESC;

	public GetPlayerListRequest(int pageSize, int offset, Boolean isDESC) {
		this.pageSize = pageSize;
		this.offset = offset;
		this.isDESC = isDESC;
	}

	@Override
	public PacketType getType() {
		return PacketType.REQUEST;
	}
}
