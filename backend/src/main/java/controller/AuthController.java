import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.HashMap;

/**
 * AuthController — Handles /api/auth/* endpoints.
 */
public class AuthController {

    private final AuthenticationService authService = new AuthenticationService();

    public void handle(HttpExchange exchange, String path, String method) throws IOException {
        switch (method + " " + path) {
            case "POST /api/auth/register" -> handleRegister(exchange);
            case "POST /api/auth/login"    -> handleLogin(exchange);
            case "POST /api/auth/logout"   -> handleLogout(exchange);
            case "GET /api/auth/me"        -> handleMe(exchange);
            default                        -> Router.sendError(exchange, 404, "Endpoint not found");
        }
    }

    // ─── POST /api/auth/register ──────────────────────────────────────────────

    private void handleRegister(HttpExchange exchange) throws IOException {
        String body = Router.readBody(exchange);

        String name       = JsonUtil.extractValue(body, "name");
        String email      = JsonUtil.extractValue(body, "email");
        String password   = JsonUtil.extractValue(body, "password");
        String phone      = JsonUtil.extractValue(body, "phone");
        String department = JsonUtil.extractValue(body, "department");
        int    year       = JsonUtil.extractInt(body, "year", 0);

        Map<String, Object> result = authService.register(name, email, password, phone, department, year);

        if (result.containsKey("error")) {
            Router.sendJson(exchange, 400, JsonUtil.error((String) result.get("error")));
        } else {
            Map<String, Object> data = new HashMap<>();
            data.put("token",      result.get("token"));
            data.put("userId",     result.get("userId"));
            data.put("name",       result.get("name"));
            data.put("role",       result.get("role"));
            Router.sendJson(exchange, 201, JsonUtil.data(JsonUtil.mapToJson(data)));
        }
    }

    // ─── POST /api/auth/login ─────────────────────────────────────────────────

    private void handleLogin(HttpExchange exchange) throws IOException {
        String body     = Router.readBody(exchange);
        String email    = JsonUtil.extractValue(body, "email");
        String password = JsonUtil.extractValue(body, "password");

        Map<String, Object> result = authService.login(email, password);

        if (result.containsKey("error")) {
            Router.sendJson(exchange, 401, JsonUtil.error((String) result.get("error")));
        } else {
            Router.sendJson(exchange, 200, JsonUtil.data(JsonUtil.mapToJson(result)));
        }
    }

    // ─── POST /api/auth/logout ────────────────────────────────────────────────

    private void handleLogout(HttpExchange exchange) throws IOException {
        String token = Router.extractToken(exchange);
        authService.logout(token);
        Router.sendJson(exchange, 200, JsonUtil.success("Logged out successfully."));
    }

    // ─── GET /api/auth/me ─────────────────────────────────────────────────────

    private void handleMe(HttpExchange exchange) throws IOException {
        String token = Router.extractToken(exchange);
        User user = authService.getCurrentUser(token);
        if (user == null) {
            Router.sendJson(exchange, 401, JsonUtil.error("Not authenticated."));
        } else {
            Router.sendJson(exchange, 200, JsonUtil.data(user.toJson()));
        }
    }
}
