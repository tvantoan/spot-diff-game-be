package imggame.game;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import imggame.models.User;
import imggame.utils.GameHelper;

public class GameRoom {
	private String id;
	private String roomName;
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

	public GameRoom() {
		this.id = UUID.randomUUID().toString();
		this.roomName = "Room " + this.id.substring(0, 8);
		this.imageSet = null;
		this.pointLeftToGuess = 0;
		this.state = GameState.WAITING;
		this.timerService = Executors.newScheduledThreadPool(1);
	}

	public GameRoom(Player p1, Player p2) {
		this.id = UUID.randomUUID().toString();
		this.roomName = "Room " + this.id.substring(0, 8);
		this.imageSet = GameHelper.getRandomImageSet();
		this.player1 = p1;
		this.player2 = p2;
		this.pointLeftToGuess = imageSet.getTotalDifferences();
		this.player1.isTurn = true;
		this.player2.isTurn = false;
		this.state = GameState.READY;
		this.timerService = Executors.newScheduledThreadPool(1);
	}

	public GameRoom(Player p1) {
		this.id = UUID.randomUUID().toString();
		this.roomName = "Room " + this.id.substring(0, 8);
		this.imageSet = GameHelper.getRandomImageSet();
		this.player1 = p1;
		this.player1.isTurn = true;
		this.pointLeftToGuess = imageSet.getTotalDifferences();
		this.state = GameState.WAITING;
		this.timerService = Executors.newScheduledThreadPool(1);
	}

	public GameRoom(Player p1, String roomName) {
		this.id = UUID.randomUUID().toString();
		this.roomName = roomName != null && !roomName.isEmpty() ? roomName : "Room " + this.id.substring(0, 8);
		this.imageSet = GameHelper.getRandomImageSet();
		this.player1 = p1;
		this.player1.isTurn = true;
		this.pointLeftToGuess = imageSet.getTotalDifferences();
		this.state = GameState.WAITING;
		this.timerService = Executors.newScheduledThreadPool(1);
	}

	public String getId() {
		return this.id;
	}

	public String getRoomName() {
		return this.roomName;
	}

	public void setRoomName(String roomName) {
		if (roomName != null && !roomName.isEmpty()) {
			this.roomName = roomName;
		}
	}

	public int getTotalPlayers() {
		int count = 0;
		if (this.player1 != null)
			count++;
		if (this.player2 != null)
			count++;
		return count;
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

	public boolean addPlayer(Player p) {
		if (this.state != GameState.PLAYING && this.player2 == null) {
			this.player2 = p;
			this.player2.isTurn = false;
			this.state = GameState.READY;
			return true;
		}

		if (this.state != GameState.PLAYING && this.player1 == null) {
			this.player1 = p;
			this.player1.isTurn = false;
			this.state = GameState.READY;
			return true;
		}
		return false;
	}

	public Player getOpponent(int userId) {
		if (player1 != null && player1.info.getId() == userId) {
			return player2;
		} else if (player2 != null && player2.info.getId() == userId) {
			return player1;
		}
		return null;
	}

	public boolean isAllReady() {
		return (player1 != null && player1.isReady) && (player2 != null && player2.isReady);
	}

	public Player getPlayerById(int userId) {
		if (player1 != null && player1.info.getId() == userId) {
			return player1;
		} else if (player2 != null && player2.info.getId() == userId) {
			return player2;
		}
		return null;
	}

	public boolean isFull() {
		return this.player1 != null && this.player2 != null;
	}

	public boolean isEmpty() {
		return this.player1 == null && this.player2 == null;
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
		if (this.player1 != null) {
			this.player1.timer = MAX_TIME_PER_GUESS;
		}
		if (this.player2 != null) {
			this.player2.timer = MAX_TIME_PER_GUESS;
		}
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
		if (this.player1 != null && this.player1.isTurn) {
			this.player1.timer--;
			System.out.println("Player1 timer: " + this.player1.timer);
			return this.player1.timer;
		}
		if (this.player2 != null && this.player2.isTurn) {
			this.player2.timer--;
			System.out.println("Player2 timer: " + this.player2.timer);
			return this.player2.timer;
		}
		return -1;
	}

	public boolean isGameOver() {
		return pointLeftToGuess <= 0;
	}

	public Player getCurrentPlayer() {
		return this.player1.isTurn ? this.player1 : this.player2;
	}

	public User getWinner() {
		if (this.player1.score == this.player2.score) {
			return null;
		}
		if (this.player1.score > this.player2.score)
			return this.player1.info;
		else if (this.player2.score > this.player1.score)
			return this.player2.info;
		else
			return null;
	}

	public User getLoser() {
		if (this.player1.score == this.player2.score) {
			return null;
		}
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
			switchTurn();
			resetTimer();
			return null;
		}
		this.pointLeftToGuess--;
		if (this.player1.isTurn) {
			this.player1.score += calcPointForGuess(this.player1.timer);
		} else {
			this.player2.score += calcPointForGuess(this.player2.timer);
		}
		switchTurn();
		resetTimer();
		return hitBox;
	}

	private int calcPointForGuess(int timeLeft) {
		return (int) (MIN_POINT_PER_GUESS + (double) ((double) timeLeft / MAX_TIME_PER_GUESS) * TIME_FACTOR);
	}

	public void start() {
		System.out.println("starting game...");
		if (!this.isFull()) {
			System.out.println("Room is not full. Cannot start.");
			return;
		}

		// Tạo timerService mới nếu đã bị shutdown trước đó
		if (timerService == null || timerService.isShutdown()) {
			System.out.println("Creating new timer service...");
			timerService = Executors.newScheduledThreadPool(1);
		}

		System.out.println("GameRoom " + this.id + " started.");
		this.state = GameState.PLAYING;
		this.startTime = System.currentTimeMillis();
		this.resetTimer();
		this.player1.isTurn = true;
		this.player2.isTurn = false;

		System.out.println("Timer initialized - Player1: " + player1.timer + ", Player2: " + player2.timer);
		System.out.println("Current turn - Player1: " + player1.isTurn + ", Player2: " + player2.isTurn);

		timerTask = timerService.scheduleAtFixedRate(() -> {
			try {
				int timeLeft = this.decreaseTimer();
				System.out.println("Time left: " + timeLeft);
				if (timeLeft <= 0) {
					System.out.println("Time's up! Switching turn...");
					switchTurn();
					resetTimer();
				}
			} catch (Exception e) {
				System.err.println("Error in timer task: " + e.getMessage());
				e.printStackTrace();
			}
		}, 1, 1, TimeUnit.SECONDS);

		System.out.println("Timer task scheduled successfully");
	}

	public void resetState() {
		this.state = GameState.READY;
		this.imageSet = GameHelper.getRandomImageSet();
		this.pointLeftToGuess = imageSet.getTotalDifferences();
		if (player1 != null) {
			this.player1.isReady = false;
			this.player1.timer = 0;
			this.player1.score = 0;
		}
		if (player2 != null) {
			this.player2.isReady = false;
			this.player2.timer = 0;
			this.player2.score = 0;
		}
		this.resetTimer();
		if (timerTask != null) {
			timerTask.cancel(false);
		}
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
		System.out.println("Player " + userId + " is leaving room " + this.id);

		// Xóa player khỏi phòng
		if (player1 != null && player1.info.getId() == userId) {
			player1 = null;
			System.out.println("Removed player1");
		} else if (player2 != null && player2.info.getId() == userId) {
			player2 = null;
			System.out.println("Removed player2");
		}

		// Nếu còn player, đặt trạng thái về WAITING
		if (!isEmpty()) {
			this.state = GameState.WAITING;
			System.out.println("Room now in WAITING state");
		} else {
			System.out.println("Room is now empty");
		}
		this.resetState();
	}

	public void endGame() {
		System.out.println("Ending game for room " + this.id);
		this.state = GameState.FINISHED;
		this.endTime = System.currentTimeMillis();
		this.resetState();

		// Chỉ cancel task, KHÔNG shutdown timerService để có thể tái sử dụng
		if (timerTask != null) {
			timerTask.cancel(false);
			timerTask = null;
			System.out.println("Timer task cancelled");
		}

		// KHÔNG shutdown timerService ở đây nữa - sẽ được shutdown khi room bị xóa
		System.out.println("Game ended successfully");
	}

	// Method cleanup để gọi khi room bị xóa hoàn toàn
	public void cleanup() {
		System.out.println("Cleaning up room " + this.id);
		if (timerTask != null) {
			timerTask.cancel(false);
			timerTask = null;
		}
		if (timerService != null && !timerService.isShutdown()) {
			timerService.shutdown();
			System.out.println("Timer service shutdown");
		}
	}

}
