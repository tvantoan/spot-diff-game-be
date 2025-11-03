package imggame.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import imggame.database.Database;
import imggame.models.User;

public class UserRepository {
	private final Connection connection;

	public UserRepository() {
		this.connection = Database.getConnection();
	}

	public User findById(int id) {
		String query = "SELECT * FROM users WHERE id = ?";
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setInt(1, id);
			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					return mapResultSetToUser(resultSet);
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error finding user by ID: " + id, e);
		}
		return null;
	}

	public User findByUsername(String username) {
		String query = "SELECT * FROM users WHERE username = ?";
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setString(1, username);
			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					return mapResultSetToUser(resultSet);
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error finding user by username: " + username, e);
		}
		return null;
	}

	public User findByEmail(String email) {
		String query = "SELECT * FROM users WHERE email = ?";
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setString(1, email);
			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					return mapResultSetToUser(resultSet);
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error finding user by email: " + email, e);
		}
		return null;
	}

	public User save(User user) {
		String query = "INSERT INTO users (username, email, password, is_online, is_in_game, score, elo) VALUES (?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
			statement.setString(1, user.getUsername());
			statement.setString(2, user.getEmail());
			statement.setString(3, user.getPassword());
			statement.setBoolean(4, user.isOnline());
			statement.setBoolean(5, user.isInGame());
			statement.setInt(6, user.getScore());
			statement.setInt(7, user.getElo());

			int affectedRows = statement.executeUpdate();
			if (affectedRows == 0) {
				throw new RuntimeException("Creating user failed, no rows affected.");
			}

			try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
				if (generatedKeys.next()) {
					user.setId(generatedKeys.getInt(1));
				} else {
					throw new RuntimeException("Creating user failed, no ID obtained.");
				}
			}
			return user;
		} catch (SQLException e) {
			throw new RuntimeException("Error creating user", e);
		}
	}

	public void update(User user) {
		String query = "UPDATE users SET username = ?, email = ?, password = ?, is_online = ?, is_in_game = ?, score = ?, elo = ? WHERE id = ?";
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setString(1, user.getUsername());
			statement.setString(2, user.getEmail());
			statement.setString(3, user.getPassword());
			statement.setBoolean(4, user.isOnline());
			statement.setBoolean(5, user.isInGame());
			statement.setInt(6, user.getScore());
			statement.setInt(7, user.getElo());
			statement.setInt(8, user.getId());

			int affectedRows = statement.executeUpdate();
			if (affectedRows == 0) {
				throw new RuntimeException("Updating user failed, no rows affected.");
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error updating user", e);
		}
	}

	public boolean deleteById(int id) {
		String query = "DELETE FROM users WHERE id = ?";
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setInt(1, id);
			return statement.executeUpdate() > 0;
		} catch (SQLException e) {
			throw new RuntimeException("Error deleting user by ID: " + id, e);
		}
	}

	public List<User> findRankedUsers(int limit, int offset, boolean descending) {
		String orderDirection = descending ? "DESC" : "ASC";
		String query = "SELECT * FROM users ORDER BY elo " + orderDirection + " LIMIT ? OFFSET ?";
		List<User> users = new ArrayList<>();

		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setInt(1, limit);
			statement.setInt(2, offset);

			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					users.add(mapResultSetToUserWithoutPassword(resultSet));
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error fetching ranked users", e);
		}
		return users;
	}

	public int countAll() {
		String query = "SELECT COUNT(*) FROM users";
		try (PreparedStatement statement = connection.prepareStatement(query);
				ResultSet resultSet = statement.executeQuery()) {
			if (resultSet.next()) {
				return resultSet.getInt(1);
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error counting users", e);
		}
		return 0;
	}

	public void updateOnlineStatus(int userId, boolean isOnline) {
		String query = "UPDATE users SET is_online = ? WHERE id = ?";
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setBoolean(1, isOnline);
			statement.setInt(2, userId);
			statement.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException("Error updating online status", e);
		}
	}

	public void updateInGameStatus(int userId, boolean isInGame) {
		String query = "UPDATE users SET is_in_game = ? WHERE id = ?";
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setBoolean(1, isInGame);
			statement.setInt(2, userId);
			statement.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException("Error updating in-game status", e);
		}
	}

	public List<User> findOnlineUsers() {
		String query = "SELECT * FROM users WHERE is_online = TRUE";
		List<User> users = new ArrayList<>();
		try (PreparedStatement statement = connection.prepareStatement(query);
				ResultSet resultSet = statement.executeQuery()) {
			while (resultSet.next()) {
				users.add(mapResultSetToUserWithoutPassword(resultSet));
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error fetching online users", e);
		}
		return users;
	}

	private User mapResultSetToUser(ResultSet resultSet) throws SQLException {
		User user = new User(
				resultSet.getInt("id"),
				resultSet.getString("username"),
				resultSet.getString("email"),
				resultSet.getString("password"),
				resultSet.getInt("score"),
				resultSet.getInt("elo"));
		user.setOnline(resultSet.getBoolean("is_online"));
		user.setInGame(resultSet.getBoolean("is_in_game"));
		return user;
	}

	private User mapResultSetToUserWithoutPassword(ResultSet resultSet) throws SQLException {
		User user = new User(
				resultSet.getInt("id"),
				resultSet.getString("username"),
				resultSet.getString("email"),
				resultSet.getInt("score"),
				resultSet.getInt("elo"));
		user.setOnline(resultSet.getBoolean("is_online"));
		user.setInGame(resultSet.getBoolean("is_in_game"));
		return user;
	}

}
