package imggame.game;

import java.io.Serializable;

import imggame.models.User;

public class Player implements Serializable {
	private static final long serialVersionUID = 1L;

	public User info;
	public int score;
	public boolean isTurn;
	public int timer;
	public boolean isReady;

	public Player(User user) {
		this.info = user;
		this.isTurn = false;
		this.score = 0;
		this.isReady = false;
	}
}
