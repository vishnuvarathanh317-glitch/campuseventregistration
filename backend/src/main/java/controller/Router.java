import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Router — Central HTTP request dispatcher.
 *
 * Maps incoming URI paths to the correct controller.
 * Also handles CORS preflight, static utility methods (readBody, sendJson, etc.)
 */
public class Router implements HttpHandler {

    private final AuthController         authController  = new AuthController();
    private final EventController        eventController = new EventController();
    private final RegistrationController regController   = new RegistrationController();
    private final AdminController        adminController = new AdminController();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // ── CORS headers (allow frontend on any localhost port) ───────────────
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers",
                "Content-Type, Authorization");

        // Handle OPTIONS preflight
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String method = exchange.getRequestMethod().toUpperCase();
        String path   = exchange.getRequestURI().getPath();
        // Remove trailing slash
        if (path.endsWith("/") && path.length() > 1) path = path.substring(0, path.length() - 1);

        Map<String, String> queryParams = parseQuery(exchange.getRequestURI().getQuery());

        try {
            // ── /api/auth/* ─────────────────────────────────────────────────
            if (path.startsWith("/api/auth/")) {
                authController.handle(exchange, path, method);
                return;
            }

            // ── /api/admin/* ────────────────────────────────────────────────
            if (path.startsWith("/api/admin/")) {
                adminController.handle(exchange, path, method, queryParams);
                return;
            }

            // ── /api/events/{id}/register ────────────────────────────────────
            if (path.matches("/api/events/\\d+/register") && method.equals("POST")) {
                int eventId = extractMidSegmentId(path); // /api/events/{id}/register
                regController.handleRegister(exchange, eventId);
                return;
            }

            // ── /api/events/{id} or /api/events ─────────────────────────────
            if (path.startsWith("/api/events")) {
                String pathParam = null;
                if (path.matches("/api/events/\\d+")) {
                    pathParam = path.substring("/api/events/".length());
                }
                eventController.handle(exchange, path.equals("/api/events") ? "/api/events" : path,
                        method, pathParam);
                return;
            }

            // ── /api/registrations/my ────────────────────────────────────────
            if (path.equals("/api/registrations/my") && method.equals("GET")) {
                regController.handleMyRegistrations(exchange);
                return;
            }

            // ── /api/registrations/{id} ──────────────────────────────────────
            if (path.matches("/api/registrations/\\d+") && method.equals("DELETE")) {
                int regId = Integer.parseInt(path.substring("/api/registrations/".length()));
                regController.handleCancel(exchange, regId);
                return;
            }

            // ── 404 ──────────────────────────────────────────────────────────
            sendError(exchange, 404, "Endpoint not found: " + method + " " + path);

        } catch (Exception e) {
            System.err.println("[Router] Unhandled error on " + path + ": " + e.getMessage());
            e.printStackTrace();
            sendError(exchange, 500, "Internal server error.");
        }
    }

    // ─── Static helpers (used by all controllers) ────────────────────────────

    public static String readBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public static void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    public static void sendError(HttpExchange exchange, int status, String message) throws IOException {
        sendJson(exchange, status, JsonUtil.error(message));
    }

    public static String extractToken(HttpExchange exchange) {
        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7).trim();
        }
        return null;
    }

    public static Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isBlank()) return map;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                try {
                    map.put(java.net.URLDecoder.decode(kv[0], "UTF-8"),
                            java.net.URLDecoder.decode(kv[1], "UTF-8"));
                } catch (Exception e) {
                    map.put(kv[0], kv[1]);
                }
            }
        }
        return map;
    }

    /** Extracts the numeric segment before the last path component.
     *  e.g. /api/events/42/register → 42  */
    private static int extractMidSegmentId(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[parts.length - 2]);
    }
}
