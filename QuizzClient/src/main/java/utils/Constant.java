package utils;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.stage.Screen;

public final class Constant {
	
	public final class MySQLProperties {
		public static final String DRIVER = "com.mysql.cj.jdbc.Driver";
		public static final String URL = readConfig("DB_URL", "jdbc:mysql://localhost:3306/QuizzDB");
		public static final String USERNAME = readConfig("DB_USERNAME", "root");
		public static final String PASSWORD = readConfig("DB_PASSWORD", "");
	}

	public final static class ScreenSize {
		public static final double WIDTH = Screen.getPrimary().getVisualBounds().getWidth();
		public static final double HEIGHT = Screen.getPrimary().getVisualBounds().getHeight();
	}

	public final static class KeyMap {
		public static final KeyCodeCombination FullScreenKey = new KeyCodeCombination(KeyCode.F11);
	}

	private static String readConfig(String key, String defaultValue) {
		String systemValue = System.getProperty(key);
		if (systemValue != null && !systemValue.isBlank()) {
			return systemValue;
		}

		String envValue = System.getenv(key);
		if (envValue != null && !envValue.isBlank()) {
			return envValue;
		}

		return defaultValue;
	}

}
