package imggame.controllers;

import imggame.models.User;
import imggame.network.packets.ErrorResponse;
import imggame.network.packets.GetPlayerListRequest;
import imggame.network.packets.LoginRequest;
import imggame.network.packets.RegisterRequest;
import imggame.repository.UserRepository;
import java.util.List;

public class UserController {
	private UserRepository userRepository;

	public UserController() {
		this.userRepository = new UserRepository();
	}

	public Object handleLogin(LoginRequest loginRequest) {
		User user = userRepository.findByUsername(loginRequest.username);
		if (user == null) {
			return new ErrorResponse("User not found");
		}
		if (loginRequest.password.equals(user.getPassword())) {
			user.setOnline(true);
			userRepository.updateOnlineStatus(user.getId(), true);
			return user;
		} else {
			return new ErrorResponse("Invalid password");
		}
	}

	public Object handleRegister(RegisterRequest registerRequest) {
		User existingUser = userRepository.findByUsername(registerRequest.username);
		if (existingUser != null) {
			return new ErrorResponse("Username already exists");
		}

		existingUser = userRepository.findByEmail(registerRequest.email);
		if (existingUser != null) {
			return new ErrorResponse("Email already registered");
		}

		User newUser = new User(registerRequest.username, registerRequest.email, registerRequest.password);

		userRepository.save(newUser);
		return newUser;
	}

	public List<User> handleGetPlayerList(GetPlayerListRequest request) {
		List<User> users = userRepository.findRankedUsers(request.pageSize, request.offset, request.isDESC);
		return users;
	}

	public User getUserById(int userId) {
		return userRepository.findById(userId);
	}
}
