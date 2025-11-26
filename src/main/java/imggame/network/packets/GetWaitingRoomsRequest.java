package imggame.network.packets;

import imggame.network.types.PacketType;

// request
public class GetWaitingRoomsRequest extends BasePacket { 	public PacketType getType() {
    return PacketType.REQUEST;
} }

