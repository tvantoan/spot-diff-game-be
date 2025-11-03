
package imggame.models;

import java.io.Serializable;

public class User implements Serializable {
	private int id;
	private String username;
	private String email;
	private String password;
	private boolean isOnline;
	private boolean isInGame;
	private int score;
	private int elo;

	public User(int id, String username, String email, String password, int score, int elo) {
		this.id = id;
		this.username = username;
		this.email = email;
		this.password = password;
		this.score = score;
		this.elo = elo;
		this.isInGame = false;
		this.isOnline = false;
	}

	public User(int id, String username, String email, int score, int elo) {
		this.id = id;
		this.username = username;
		this.email = email;
		this.score = score;
		this.elo = elo;
		this.isInGame = false;
		this.isOnline = false;
	}

	public User(String username, String email, String password) {
		this.username = username;
		this.email = email;
		this.password = password;
		this.score = 0;
		this.elo = 1000;
		this.isInGame = false;
		this.isOnline = false;
	}

	public boolean isOnline() {
		return isOnline;
	}

	public boolean isInGame() {
		return isInGame;
	}

	public void setOnline(boolean isOnline) {
		this.isOnline = isOnline;
	}

	public void setInGame(boolean isInGame) {
		this.isInGame = isInGame;
	}

	public int getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public String getEmail() {
		return email;
	}

	public String getPassword() {
		return password;
	}

	public int getScore() {
		return score;
	}

	public int getElo() {
		return elo;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public void setElo(int elo) {
		this.elo = elo;
	}

}
