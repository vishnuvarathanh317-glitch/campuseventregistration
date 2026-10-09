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
        // 1. Check all common environment variable names (Render, Railway, Heroku, TiDB, Aiven, Clever Cloud)
        String envUrl  = getFirstEnv("DB_URL", "DATABASE_URL", "MYSQL_URL", "JAWSDB_URL", "CLEARDB_DATABASE_URL");
        String envHost = getFirstEnv("DB_HOST", "MYSQLHOST", "MYSQL_HOST");
        String envPort = getFirstEnv("DB_PORT", "MYSQLPORT", "MYSQL_PORT");
        String envName = getFirstEnv("DB_NAME", "MYSQLDATABASE", "MYSQL_DATABASE", "DB_DATABASE");
        String envUser = getFirstEnv("DB_USERNAME", "DB_USER", "MYSQLUSER", "MYSQL_USER");
        String envPass = getFirstEnv("DB_PASSWORD", "DB_PASS", "MYSQLPASSWORD", "MYSQL_PASSWORD");

        if (envUrl != null && !envUrl.isBlank()) {
            parseAndSetDbUrl(envUrl.trim(), envUser, envPass);
            System.out.println("[DB] Configured from DB_URL / DATABASE_URL: " + this.url);
            return;
        }

        if (envHost != null && !envHost.isBlank()) {
            String host = envHost.trim();
            String port = (envPort != null && !envPort.isBlank()) ? envPort.trim() : "3306";
            String name = (envName != null && !envName.isBlank()) ? envName.trim() : "campus_events";
            this.username = envUser != null ? envUser.trim() : "root";
            this.password = envPass != null ? envPass.trim() : "";
            this.url = "jdbc:mysql://" + host + ":" + port + "/" + name
                     + "?sslMode=PREFERRED&serverTimezone=UTC&allowPublicKeyRetrieval=true&connectTimeout=10000&socketTimeout=30000";
            System.out.println("[DB] Loaded from env vars: host=" + host + ":" + port + ", db=" + name + ", user=" + this.username);
            return;
        }

        // 2. Fall back to config.properties (local development)
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

    public synchronized Connection getConnection() throws SQLException {
        boolean needNewConnection = false;
        try {
            if (connection == null || connection.isClosed() || !connection.isValid(2)) {
                needNewConnection = true;
            }
        } catch (SQLException e) {
            needNewConnection = true;
        }

        if (needNewConnection) {
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

    private String getFirstEnv(String... keys) {
        for (String key : keys) {
            String val = System.getenv(key);
            if (val != null && !val.isBlank()) return val;
        }
        return null;
    }

    private void parseAndSetDbUrl(String rawUrl, String defaultUser, String defaultPass) {
        String cleanUrl = rawUrl;
        if (cleanUrl.startsWith("mysql://")) {
            try {
                java.net.URI uri = new java.net.URI(cleanUrl);
                String userInfo = uri.getUserInfo();
                if (userInfo != null && userInfo.contains(":")) {
                    String[] parts = userInfo.split(":", 2);
                    this.username = parts[0];
                    this.password = parts[1];
                } else if (userInfo != null) {
                    this.username = userInfo;
                    this.password = defaultPass != null ? defaultPass : "";
                } else {
                    this.username = defaultUser != null ? defaultUser : "";
                    this.password = defaultPass != null ? defaultPass : "";
                }
                int port = uri.getPort() > 0 ? uri.getPort() : 3306;
                String path = uri.getPath() != null && uri.getPath().length() > 1 ? uri.getPath() : "/campus_events";
                this.url = "jdbc:mysql://" + uri.getHost() + ":" + port + path
                         + "?sslMode=PREFERRED&serverTimezone=UTC&allowPublicKeyRetrieval=true&connectTimeout=10000&socketTimeout=30000";
                return;
            } catch (Exception e) {
                cleanUrl = "jdbc:" + rawUrl;
            }
        }
        if (!cleanUrl.startsWith("jdbc:")) {
            cleanUrl = "jdbc:mysql://" + cleanUrl;
        }
        if (!cleanUrl.contains("?")) {
            cleanUrl += "?sslMode=PREFERRED&serverTimezone=UTC&allowPublicKeyRetrieval=true&connectTimeout=10000&socketTimeout=30000";
        }
        this.url = cleanUrl;
        this.username = defaultUser != null ? defaultUser : "";
        this.password = defaultPass != null ? defaultPass : "";
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
