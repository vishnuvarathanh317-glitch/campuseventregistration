import com.sun.net.httpserver.HttpServer;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.concurrent.Executors;

/**
 * Main — Application entry point.
 *
 * Starts the built-in Java HttpServer (com.sun.net.httpserver).
 * No external framework required.
 *
 * Usage: java -cp "out;lib/mysql-connector-j-9.3.0.jar" Main
 */
public class Main {

    public static void main(String[] args) throws Exception {
        // ── Load config ───────────────────────────────────────────────────────
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            props.load(fis);
        } catch (Exception e) {
            System.err.println("[WARN] Could not load config.properties, using defaults.");
        }
        // ── Determine port (env var PORT takes priority for Render/cloud hosting) ──
        int port;
        String envPort = System.getenv("PORT");
        if (envPort != null && !envPort.isBlank()) {
            port = Integer.parseInt(envPort.trim());
        } else {
            port = Integer.parseInt(props.getProperty("server.port", "8080"));
        }

        // ── Test DB connection early ───────────────────────────────────────────
        System.out.println("================================================");
        System.out.println("  Campus Event Registration System");
        System.out.println("================================================");
        System.out.println("[DB] Connecting to MySQL...");
        try {
            DatabaseConnection.getInstance().getConnection();
            System.out.println("[DB] Connected successfully.");
        } catch (Exception e) {
            System.err.println("[DB] Initial connection warning: " + e.getMessage());
            System.err.println("     → Ensure MySQL is running or env vars (DB_HOST, DB_USER, DB_PASSWORD) are set.");
            System.err.println("     → Server will continue running and retry on incoming requests.");
        }

        // ── Create HTTP server ─────────────────────────────────────────────────
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // Single handler for all /api/* routes
        Router router = new Router();
        server.createContext("/api", router);

        // Thread pool for concurrent requests
        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();

        System.out.println("[SERVER] Running at http://localhost:" + port);
        System.out.println("[SERVER] API base: http://localhost:" + port + "/api");
        System.out.println("[SERVER] Press Ctrl+C to stop.");
        System.out.println("================================================");

        // ── Session cleanup scheduler ─────────────────────────────────────────
        Thread cleanupThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(10 * 60 * 1000L); // every 10 minutes
                    SessionManager.getInstance().cleanExpiredSessions();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        cleanupThread.setDaemon(true);
        cleanupThread.start();

        // ── Graceful shutdown hook ────────────────────────────────────────────
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[SERVER] Shutting down...");
            server.stop(2);
            DatabaseConnection.getInstance().closeConnection();
            System.out.println("[SERVER] Goodbye.");
        }));
    }
}
