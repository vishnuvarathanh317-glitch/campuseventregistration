import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * DatabaseConnection — Singleton pattern.
 * Loads credentials from config.properties (never hardcoded).
 * Provides a single shared JDBC connection with auto-reconnect.
 */
public class DatabaseConnection {

    private static DatabaseConnection instance;
    private Connection connection;

    private String url;
    private String username;
    private String password;

    private DatabaseConnection() {
        loadConfig();
    }

    private void loadConfig() {
        Properties props = new Properties();
        File configFile = new File("config.properties");
        if (!configFile.exists()) {
            configFile = new File("backend/config.properties");
        }

        try (FileInputStream fis = new FileInputStream(configFile)) {
            props.load(fis);
            String host = props.getProperty("db.host", "localhost").trim();
            String port = props.getProperty("db.port", "3306").trim();
            String name = props.getProperty("db.name", "campus_events").trim();
            this.username = props.getProperty("db.username", "root").trim();
            this.password = props.getProperty("db.password", "").trim();
            this.url = "jdbc:mysql://" + host + ":" + port + "/" + name
                     + "?useSSL=false&serverTimezone=Asia/Kolkata&allowPublicKeyRetrieval=true";
            System.out.println("[DB] Loaded config: user=" + this.username + ", db=" + name + ", host=" + host + ":" + port);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties: " + e.getMessage(), e);
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(url, username, password);
                System.out.println("[DB] Connection established successfully.");
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC Driver not found. Place mysql-connector-j jar in lib/", e);
            }
        }
        return connection;
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("[DB] Connection closed.");
            } catch (SQLException e) {
                System.err.println("[DB] Error closing connection: " + e.getMessage());
            }
        }
    }
}
