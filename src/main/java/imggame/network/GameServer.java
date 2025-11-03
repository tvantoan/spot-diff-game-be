package imggame.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import imggame.config.AppConfig;
import imggame.controllers.GameController;
import imggame.controllers.UserController;
import imggame.game.GameRoom;
import imggame.game.Player;
import imggame.models.User;
import imggame.network.packets.BasePacket;
import imggame.network.packets.CreateGameRoomRequest;
import imggame.network.packets.ErrorResponse;
import imggame.network.packets.GameEndNotification;
import imggame.network.packets.GameRoomResponse;
import imggame.network.packets.GameStartNotification;
import imggame.network.packets.GameStateUpdateNotification;
import imggame.network.packets.GetPlayerListRequest;
import imggame.network.packets.GuessPointRequest;
import imggame.network.packets.GuessPointResponse;
import imggame.network.packets.JoinGameRoomRequest;
import imggame.network.packets.LeaveGameRoomRequest;
import imggame.network.packets.LoginRequest;
import imggame.network.packets.RegisterRequest;
import imggame.network.packets.StartGameRequest;

public class GameServer {
	private ServerSocket serverSocket;
	private UserController userController = new UserController();
	private GameController gameController = new GameController();
	private ExecutorService threadPool;
	private ScheduledExecutorService timerScheduler;
	private ScheduledExecutorService cleanupScheduler;
	private Map<Integer, ClientHandler> connectedClients = new ConcurrentHashMap<>();
	private volatile boolean running = true;

	public GameServer() {
		int serverPort = AppConfig.GAME_MAIN_PORT;
		this.threadPool = Executors.newCachedThreadPool();
		this.timerScheduler = Executors.newScheduledThreadPool(1);
		this.cleanupScheduler = Executors.newScheduledThreadPool(1);

		try {
			this.serverSocket = new ServerSocket(serverPort);
			System.out.println("Server started on port: " + serverPort);

			startTimerUpdateTask();

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error starting the server", e);
		}
	}

	public ServerSocket getServerSocket() {
		return serverSocket;
	}

	public void listen() {
		System.out.println("Server listening for connections...");

		while (running) {
			try {
				Socket clientSocket = serverSocket.accept();
				System.out.println("New client connected: " +
						clientSocket.getInetAddress().getHostAddress());

				ClientHandler handler = new ClientHandler(clientSocket);
				threadPool.submit(handler);

			} catch (IOException e) {
				if (running) {
					System.err.println("Error accepting client connection: " + e.getMessage());
				}
			}
		}
	}

	public void shutdown() {
		running = false;
		try {
			if (serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
			}
			threadPool.shutdown();

			if (timerScheduler != null && !timerScheduler.isShutdown()) {
				timerScheduler.shutdown();
			}
			if (cleanupScheduler != null && !cleanupScheduler.isShutdown()) {
				cleanupScheduler.shutdown();
			}

			connectedClients.values().forEach(ClientHandler::close);
			connectedClients.clear();

			System.out.println("Server shutdown complete");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void startTimerUpdateTask() {
		timerScheduler.scheduleAtFixedRate(() -> {
			try {
				gameController.getRoomManager().getAllRooms().forEach(room -> {
					if (room.getState() == GameRoom.GameState.PLAYING && room.isFull()) {
						Player currentPlayer = room.getCurrentPlayer();
						GameStateUpdateNotification update = new GameStateUpdateNotification(
								room.getId(),
								currentPlayer.info.getId(),
								currentPlayer.timer,
								room.getPlayer1().score,
								room.getPlayer2().score);

						sendToClient(room.getPlayer1().info.getId(), update);
						sendToClient(room.getPlayer2().info.getId(), update);
					}
				});
			} catch (Exception e) {
				System.err.println("Error in timer update task: " + e.getMessage());
			}
		}, 1, 1, TimeUnit.SECONDS);
	}

	private class ClientHandler implements Runnable {
		private Socket socket;
		private ObjectInputStream input;
		private ObjectOutputStream output;
		private Integer userId;
		private volatile boolean active = true;

		public ClientHandler(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			try {
				output = new ObjectOutputStream(socket.getOutputStream());
				output.flush();
				input = new ObjectInputStream(socket.getInputStream());

				while (active && !socket.isClosed()) {
					try {
						Object request = input.readObject();

						if (request == null) {
							break;
						}
						System.out.println("Incomming request from userId " + userId);
						processRequest(request);

					} catch (ClassNotFoundException e) {
						System.err.println("Unknown packet type: " + e.getMessage());
						sendError("Unknown packet type");
					}
				}

			} catch (IOException e) {
				if (active) {
					System.err.println("Client connection error: " + e.getMessage());
				}
			} finally {
				close();
			}
		}

		private void processRequest(Object request) {
			try {
				Object response = null;
				if (request instanceof BasePacket) {
					System.out.println("Received packet: " + ((BasePacket) request).getType());
				}

				if (request instanceof LoginRequest) {
					LoginRequest loginPacket = (LoginRequest) request;
					response = userController.handleLogin(loginPacket);
					if (!(response instanceof ErrorResponse)) {
						this.userId = ((User) response).getId();
						connectedClients.put(userId, this);
					}

				} else if (request instanceof RegisterRequest) {
					RegisterRequest registerPacket = (RegisterRequest) request;
					response = userController.handleRegister(registerPacket);

				} else if (request instanceof GetPlayerListRequest) {
					GetPlayerListRequest getPlayerListPacket = (GetPlayerListRequest) request;
					response = userController.handleGetPlayerList(getPlayerListPacket);

				} else if (request instanceof CreateGameRoomRequest) {
					CreateGameRoomRequest createRoom = (CreateGameRoomRequest) request;

					User currentUser = userController.getUserById(this.userId);
					if (currentUser == null) {
						response = new ErrorResponse("User not found");
					} else {
						Player player = new Player(currentUser);
						response = gameController.handleCreateGameRoom(createRoom, player);
					}

				} else if (request instanceof JoinGameRoomRequest) {
					JoinGameRoomRequest joinRoom = (JoinGameRoomRequest) request;

					User currentUser = userController.getUserById(this.userId);
					if (currentUser == null) {
						response = new ErrorResponse("User not found");
					} else {
						Player player = new Player(currentUser);
						response = gameController.handleJoinGameRoom(joinRoom, player);

						if (!(response instanceof ErrorResponse)) {
							GameRoomResponse roomResponse = (GameRoomResponse) response;
							GameRoom room = gameController.getRoomManager().getRoom(roomResponse.getRoomId());

							if (room != null && room.getPlayer1() != null) {
								sendToClient(room.getPlayer1().info.getId(), (BasePacket) response);
							}
						}
					}

				} else if (request instanceof StartGameRequest) {
					StartGameRequest startGame = (StartGameRequest) request;
					response = gameController.handleStartGame(startGame);

					if (response instanceof GameStartNotification) {
						GameRoom room = gameController.getRoomManager().getRoom(startGame.getRoomId());

						if (room != null && room.isFull()) {
							sendToClient(room.getPlayer1().info.getId(), (BasePacket) response);
							sendToClient(room.getPlayer2().info.getId(), (BasePacket) response);
							return;
						}
					}

				} else if (request instanceof GuessPointRequest) {
					GuessPointRequest guessPoint = (GuessPointRequest) request;
					response = gameController.handleGuessPoint(guessPoint);

					GameRoom room = gameController.getRoomManager().getRoom(guessPoint.getRoomId());

					if (room != null && room.isFull()) {
						sendToClient(room.getPlayer1().info.getId(), (BasePacket) response);
						sendToClient(room.getPlayer2().info.getId(), (BasePacket) response);

						if (room.isGameOver()) {
							GameEndNotification endNotif = new GameEndNotification(
									room.getId(),
									room.getWinner(),
									room.getLoser(),
									room.getPlayer1().score,
									room.getPlayer2().score,
									room.getGameDuration());

							sendToClient(room.getPlayer1().info.getId(), endNotif);
							sendToClient(room.getPlayer2().info.getId(), endNotif);

							new Thread(() -> {
								try {
									Thread.sleep(10000);
									gameController.getRoomManager().removeRoom(room.getId());
									System.out.println("Room " + room.getId() + " removed after game ended");
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}).start();
						}

						return;
					}

				} else if (request instanceof LeaveGameRoomRequest) {
					LeaveGameRoomRequest leaveRoom = (LeaveGameRoomRequest) request;

					GameRoom room = gameController.getRoomManager().getRoomByPlayer(leaveRoom.getUserId());

					response = gameController.handleLeaveGameRoom(leaveRoom);

					if (room != null && room.isFull()) {
						int otherPlayerId = room.getPlayer1().info.getId() == leaveRoom.getUserId()
								? room.getPlayer2().info.getId()
								: room.getPlayer1().info.getId();

						sendToClient(otherPlayerId, new ErrorResponse("Opponent has left the game"));
					}

				} else {
					response = new ErrorResponse("Unknown request type");
				}

				if (response != null) {
					if (response instanceof ErrorResponse) {
						System.out.println("Sending error response: " + ((ErrorResponse) response).message);
					}
					sendResponse(response);
				}

			} catch (Exception e) {
				e.printStackTrace();
				sendError("Error processing request: " + e.getMessage());
			}
		}

		private void sendResponse(Object response) {
			try {
				synchronized (output) {
					output.writeObject(response);
					output.flush();
				}
			} catch (IOException e) {
				System.err.println("Error sending response: " + e.getMessage());
				close();
			}
		}

		private void sendError(String message) {
			sendResponse(new ErrorResponse(message));
		}

		public void close() {
			active = false;

			if (userId != null) {
				GameRoom room = gameController.getRoomManager().getRoomByPlayer(userId);
				if (room != null) {
					Player otherPlayer = null;
					if (room.getPlayer1() != null && room.getPlayer1().info.getId() == userId) {
						otherPlayer = room.getPlayer2();
					} else if (room.getPlayer2() != null) {
						otherPlayer = room.getPlayer1();
					}

					if (otherPlayer != null) {
						sendToClient(otherPlayer.info.getId(),
								new ErrorResponse("Opponent has disconnected"));
					}

					gameController.getRoomManager().playerLeaveRoom(userId);
				}

				connectedClients.remove(userId);
			}

			try {
				if (input != null)
					input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				if (output != null)
					output.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				if (socket != null && !socket.isClosed()) {
					socket.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			System.out.println("Client disconnected: " +
					(userId != null ? "User ID " + userId : socket.getInetAddress().getHostAddress()));
		}
	}

	public void sendToClient(int userId, BasePacket packet) {
		ClientHandler handler = connectedClients.get(userId);
		if (handler != null) {
			handler.sendResponse(packet);
		}
	}

	public void broadcast(BasePacket packet) {
		connectedClients.values().forEach(handler -> handler.sendResponse(packet));
	}
}
