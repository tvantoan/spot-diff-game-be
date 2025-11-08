package imggame.network.packets;

import java.io.Serializable;

import imggame.network.types.PacketType;

public abstract class BasePacket implements Serializable {
	private static final long serialVersionUID = 1L;

	abstract public PacketType getType();
}
