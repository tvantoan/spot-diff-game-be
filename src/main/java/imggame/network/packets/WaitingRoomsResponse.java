package imggame.network.packets;

import imggame.network.types.PacketType;

import java.util.List;

// response
public class WaitingRoomsResponse extends BasePacket {
    public List<GameRoomResponse> rooms;
    public WaitingRoomsResponse(List<GameRoomResponse> rooms) { this.rooms = rooms; }

    @Override
    public PacketType getType() {
        return PacketType.DIRECT_RESPONSE;
    }
}
