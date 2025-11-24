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
import imggame.network.packets.GameStateUpdateNotification;
import imggame.network.packets.GetImageRequest;
import imggame.network.packets.GetPlayerListRequest;
import imggame.network.packets.GetRoomRequest;
import imggame.network.packets.GuessPointRequest;
import imggame.network.packets.JoinGameRoomRequest;
import imggame.network.packets.LeaveGameRoomRequest;
import imggame.network.packets.LoginRequest;
import imggame.network.packets.MessagePacket;
import imggame.network.packets.RegisterRequest;
import imggame.network.packets.StartGameRequest;
import imggame.network.types.MessageContext;
import imggame.network.types.PacketType;

public class GameServer {
	private ServerSocket serverSocket;
	private UserController userController = new UserController();
	private GameController gameController = new GameController();
	private ExecutorService threadPool;
	private ScheduledExecutorService timerScheduler;
	private Map<Integer, ClientHandler> connectedClients = new ConcurrentHashMap<>();
	private volatile boolean running = true;

	public GameServer() {
		int serverPort = AppConfig.GAME_MAIN_PORT;
		this.threadPool = Executors.newCachedThreadPool();
		this.timerScheduler = Executors.newScheduledThreadPool(1);

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
				Object response = gameController.updateRoomStates();
				if (response != null) {
					GameStateUpdateNotification notification = (GameStateUpdateNotification) response;
					GameRoom room = gameController.getRoomManager().getRoom(notification.roomId);
					if (room != null) {
						int player1Id = room.getPlayer1().info.getId();
						int player2Id = room.getPlayer2().info.getId();
						sendToClient(player1Id, notification);
						sendToClient(player2Id, notification);
					}
				}

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
					System.out.println("Received packet: " + ((BasePacket) request).getClass().getName());
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
					response = gameController.handleCreateGameRoom(createRoom);
				} else if (request instanceof GetImageRequest) {
					GetImageRequest getImageRequest = (GetImageRequest) request;
					response = gameController.getImageResource(getImageRequest);

				} else if (request instanceof JoinGameRoomRequest) {
					JoinGameRoomRequest joinRoomReq = (JoinGameRoomRequest) request;
					response = gameController.handleJoinGameRoom(joinRoomReq);

				} else if (request instanceof StartGameRequest) {
					StartGameRequest startGame = (StartGameRequest) request;
					response = gameController.handleStartGame(startGame);

				} else if (request instanceof GuessPointRequest) {
					GuessPointRequest guessPoint = (GuessPointRequest) request;
					response = gameController.handleGuessPoint(guessPoint);

				} else if (request instanceof GetRoomRequest) {
					GetRoomRequest getRoomRequest = (GetRoomRequest) request;
					response = gameController.handleGetGameRoomInfo(getRoomRequest);

				} else if (request instanceof LeaveGameRoomRequest) {
					LeaveGameRoomRequest leaveRoom = (LeaveGameRoomRequest) request;
					response = gameController.handleLeaveGameRoom(leaveRoom);

					// Gửi response trước, sau đó mới xóa player khỏi room
					if (response != null) {
						if (response instanceof ErrorResponse) {
							System.out.println("Sending error response: " + ((ErrorResponse) response).message);
						}
						if (response instanceof BasePacket) {
							handleSendResponse(response);
						} else {
							sendDirectResponse(response);
						}
					}

					// SAU KHI đã gửi response, mới xóa player khỏi room
					System.out.println("Response sent, now removing player from room...");
					gameController.finalizePlayerLeave(leaveRoom.userId);

					// Set response = null để skip phần gửi response bên dưới
					response = null;

				} else if (request instanceof MessagePacket) {
					MessagePacket msgPacket = (MessagePacket) request;
					if (msgPacket.context.equals(MessageContext.GET_ROOM_LIST)) {
						response = gameController.handleGetRoomList();
					}
				}

				else {
					response = new ErrorResponse("Unknown request type");
				}

				if (response != null) {
					if (response instanceof ErrorResponse) {
						System.out.println("Sending error response: " + ((ErrorResponse) response).message);
					}
					if (response instanceof BasePacket) {
						handleSendResponse(response);
					} else {
						sendDirectResponse(response);
					}
				}

			} catch (

			Exception e) {
				e.printStackTrace();
				sendError("Error processing request: " + e.getMessage());
			}
		}

		private void sendDirectResponse(Object response) {
			try {
				System.out.println("Sending direct response to userId " + userId);
				synchronized (output) {
					output.writeObject(response);
					output.flush();
				}
			} catch (IOException e) {
				System.err.println("Error sending response: " + e.getMessage());
				close();
			}
		}

		private void handleSendResponse(Object response) {
			BasePacket res = (BasePacket) response;
			if (res.getType().equals(PacketType.DIRECT_RESPONSE)) {
				sendDirectResponse(response);
			}

			if (res.getType().equals(PacketType.ROOM_RESPONSE)) {
				GameRoom room = gameController.getRoomManager().getRoomByPlayer(userId);
				if (room != null) {
					sendToClient(this.userId, res);
					Player opponent = room.getOpponent(userId);
					if (opponent == null) {
						return;
					}
					int otherPlayerId = opponent.info.getId();
					sendToClient(otherPlayerId, res);
				}

			}
		}

		private void sendError(String message) {
			sendDirectResponse(new ErrorResponse(message));
		}

		public void close() {
			active = false;

			if (userId != null) {
				GameRoom room = gameController.getRoomManager().getRoomByPlayer(userId);
				if (room != null) {
					Object response = gameController.handlePlayerDisconnect(userId);
					handleSendResponse(response);
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

		public boolean isActive() {
			return active;
		}

	}

	public void sendToClient(int userId, BasePacket packet) {
		ClientHandler handler = connectedClients.get(userId);
		if (handler != null && handler.isActive()) {
			handler.sendDirectResponse(packet);
		}
	}
}
