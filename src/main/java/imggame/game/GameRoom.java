package imggame.game;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import imggame.models.User;

public class GameRoom {
	private String id;
	private int pointLeftToGuess;
	private Player player1;
	private Player player2;
	private int MAX_TIME_PER_GUESS = 30;
	private int MIN_POINT_PER_GUESS = 10;
	private int TIME_FACTOR = 10;
	private ImageSet imageSet;
	private ScheduledExecutorService timerService;
	private ScheduledFuture<?> timerTask;
	private GameState state;
	private long startTime;
	private long endTime;

	public enum GameState {
		WAITING,
		READY,
		PLAYING,
		PAUSED,
		FINISHED
	}

	public GameRoom(Player p1, Player p2, ImageSet imageSet) {
		this.id = UUID.randomUUID().toString();
		this.imageSet = imageSet;
		this.player1 = p1;
		this.player2 = p2;
		this.pointLeftToGuess = imageSet.getTotalDifferences();
		this.player1.isTurn = true;
		this.state = GameState.READY;
		this.timerService = Executors.newScheduledThreadPool(1);
	}

	public GameRoom(Player p1, ImageSet imageSet) {
		this.id = UUID.randomUUID().toString();
		this.imageSet = imageSet;
		this.player1 = p1;
		this.pointLeftToGuess = imageSet.getTotalDifferences();
		this.state = GameState.WAITING;
		this.timerService = Executors.newScheduledThreadPool(1);
	}

	public String getId() {
		return this.id;
	}

	public GameState getState() {
		return this.state;
	}

	public void setState(GameState state) {
		this.state = state;
	}

	public Player getPlayer1() {
		return this.player1;
	}

	public Player getPlayer2() {
		return this.player2;
	}

	public boolean addPlayer2(Player p2) {
		if (this.state == GameState.WAITING && this.player2 == null) {
			this.player2 = p2;
			this.state = GameState.READY;
			return true;
		}
		return false;
	}

	public boolean isFull() {
		return this.player1 != null && this.player2 != null;
	}

	public boolean hasPlayer(int userId) {
		return (player1 != null && player1.info.getId() == userId) ||
				(player2 != null && player2.info.getId() == userId);
	}

	public ImageSet getImageSet() {
		return this.imageSet;
	}

	public int getPointLeftToGuess() {
		return this.pointLeftToGuess;
	}

	public void resetTimer() {
		this.player1.timer = MAX_TIME_PER_GUESS;
		this.player2.timer = MAX_TIME_PER_GUESS;
	}

	public void switchTurn() {
		this.player1.isTurn = !this.player1.isTurn;
		this.player2.isTurn = !this.player2.isTurn;
	}

	public boolean isCorrectTurn(int userId) {
		if (this.player1.isTurn && this.player1.info.getId() == userId) {
			return true;
		}
		if (this.player2.isTurn && this.player2.info.getId() == userId) {
			return true;
		}
		return false;
	}

	public int decreaseTimer() {
		if (this.player1.isTurn) {
			return --this.player1.timer;
		}
		return --this.player2.timer;
	}

	public boolean isGameOver() {
		return this.pointLeftToGuess <= 0;
	}

	public Player getCurrentPlayer() {
		return this.player1.isTurn ? this.player1 : this.player2;
	}

	public User getWinner() {
		if (this.player1.score > this.player2.score)
			return this.player1.info;
		else if (this.player2.score > this.player1.score)
			return this.player2.info;
		else
			return null;
	}

	public User getLoser() {
		if (this.player1.score < this.player2.score)
			return this.player1.info;
		else if (this.player2.score < this.player1.score)
			return this.player2.info;
		else
			return null;
	}

	public DiffBox guessPoint(int userId, int x, int y) {
		if (!isCorrectTurn(userId)) {
			return null;
		}

		DiffBox hitBox = this.imageSet.getDiffBoxCollisionTo(x, y);
		if (hitBox == null) {
			return null;
		}
		this.pointLeftToGuess--;
		if (this.player1.isTurn) {
			this.player1.score += calcPointForGuess(this.player1.timer);
		} else {
			this.player2.score += calcPointForGuess(this.player2.timer);
		}
		if (pointLeftToGuess <= 0) {
			endGame();
		}
		return hitBox;
	}

	private int calcPointForGuess(int timeLeft) {
		return MIN_POINT_PER_GUESS + (timeLeft / MAX_TIME_PER_GUESS) * TIME_FACTOR;
	}

	public void start() {
		if (this.state != GameState.READY || !this.isFull()) {
			return;
		}

		this.state = GameState.PLAYING;
		this.startTime = System.currentTimeMillis();
		this.resetTimer();

		timerTask = timerService.scheduleAtFixedRate(() -> {
			int timeLeft = this.decreaseTimer();
			if (timeLeft <= 0) {
				switchTurn();
				resetTimer();
			}
		}, 1, 1, TimeUnit.SECONDS);
	}

	public void pause() {
		if (this.state == GameState.PLAYING) {
			this.state = GameState.PAUSED;
			if (timerTask != null) {
				timerTask.cancel(false);
			}
		}
	}

	public void resume() {
		if (this.state == GameState.PAUSED) {
			this.state = GameState.PLAYING;
			start();
		}
	}

	public void playerLeave(int userId) {
		if (player1 != null && player1.info.getId() == userId) {
			player1 = null;
		} else if (player2 != null && player2.info.getId() == userId) {
			player2 = null;
		}

	}

	public void endGame() {
		this.state = GameState.FINISHED;
		this.endTime = System.currentTimeMillis();

		if (timerTask != null) {
			timerTask.cancel(false);
		}

		if (timerService != null && !timerService.isShutdown()) {
			timerService.shutdown();
		}
	}

	public long getGameDuration() {
		if (endTime > 0 && startTime > 0) {
			return (endTime - startTime) / 1000;
		}
		return 0;
	}

	public void cleanup() {
		endGame();
	}

}
