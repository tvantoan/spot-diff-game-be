package imggame.game;

import imggame.models.User;

public class Player {
	public User info;
	public int score;
	public boolean isTurn;
	public int timer;

	public Player(User user) {
		this.info = user;
		this.isTurn = false;
		this.score = 0;
	}
}
