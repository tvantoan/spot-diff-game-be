package imggame.network.packets;

import imggame.network.types.PacketType;

public class GetOnlineUsersRequest extends BasePacket {
  public int userId;

  public GetOnlineUsersRequest(int userId) {
    this.userId = userId;
  }

  @Override
  public PacketType getType() {
    return PacketType.REQUEST;
  }

}
