package imggame.network.packets;

import java.io.Serializable;

class RoomResponse implements Serializable {
	private static final long serialVersionUID = 1L;
	public String roomId;
	public String roomName;
	public int playerCount;
	public int maxPlayers;

	public RoomResponse(String roomId, String roomName, int playerCount, int maxPlayers) {
		this.roomId = roomId;
		this.roomName = roomName;
		this.playerCount = playerCount;
		this.maxPlayers = maxPlayers;
	}
}
