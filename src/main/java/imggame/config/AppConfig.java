package imggame.config;

import io.github.cdimascio.dotenv.Dotenv;

public class AppConfig {
	private static final Dotenv dotenv = Dotenv.load();

	// Database configuration
	public static final String MYSQL_ROOT_PASSWORD = getEnv("MYSQL_ROOT_PASSWORD");
	public static final String MYSQL_DATABASE = getEnv("MYSQL_DATABASE");
	public static final String MYSQL_USER = getEnv("MYSQL_USER");
	public static final String MYSQL_PASSWORD = getEnv("MYSQL_PASSWORD");
	public static final String DB_HOST = getEnv("DB_HOST");
	public static final int DB_PORT = getEnvAsInt("DB_PORT", 3306);

	public static final int GAME_MAIN_PORT = getEnvAsInt("GAME_MAIN_PORT", 8080);
	public static final int MAX_TIME_PER_ROUND_SECONDS = 30;

	private static String getEnv(String key) {
		String value = dotenv.get(key);
		if (value == null) {
			throw new RuntimeException("Environment variable '" + key + "' is not set");
		}
		return value;
	}

	private static String getEnv(String key, String defaultValue) {
		String value = dotenv.get(key);
		return value != null ? value : defaultValue;
	}

	private static int getEnvAsInt(String key, int defaultValue) {
		String value = dotenv.get(key);
		if (value == null) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			throw new RuntimeException("Environment variable '" + key + "' is not a valid integer: " + value);
		}
	}

	public static String getDatabaseUrl() {
		return String.format("jdbc:mysql://%s:%d/%s?serverTimezone=UTC",
				DB_HOST, DB_PORT, MYSQL_DATABASE);
	}

	private AppConfig() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}
}
