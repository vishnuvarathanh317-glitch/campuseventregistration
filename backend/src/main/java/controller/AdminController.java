import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * AdminController — Handles /api/admin/* endpoints.
 * All routes require admin role verification.
 */
public class AdminController {

    private final AdminService        adminService = new AdminService();
    private final RegistrationService regService   = new RegistrationService();
    private final AuthenticationService authService = new AuthenticationService();

    public void handle(HttpExchange exchange, String path, String method,
                       Map<String, String> params) throws IOException {

        // Verify admin access before processing any admin route
        if (!isAdmin(exchange)) return;

        switch (path) {
            case "/api/admin/statistics"             -> handleStats(exchange);
            case "/api/admin/users"                  -> handleUsers(exchange, method);
            case "/api/admin/registrations"          -> handleRegistrations(exchange, params);
            case "/api/admin/export/registrations"   -> handleExportRegistrations(exchange, params);
            case "/api/admin/export/users"           -> handleExportUsers(exchange);
            case "/api/admin/export/events"          -> handleExportEvents(exchange);
            default -> {
                // Handle /api/admin/registrations/{id}/cancel
                if (path.matches("/api/admin/registrations/\\d+/cancel") && method.equals("PUT")) {
                    String idStr = path.replaceAll("\\D+", "").replaceFirst("^0+", "");
                    handleAdminCancelRegistration(exchange, path);
                } else if (path.matches("/api/admin/users/\\d+") && method.equals("DELETE")) {
                    handleDeleteUser(exchange, path);
                } else {
                    Router.sendError(exchange, 404, "Admin endpoint not found.");
                }
            }
        }
    }

    // ─── GET /api/admin/statistics ────────────────────────────────────────────

    private void handleStats(HttpExchange exchange) throws IOException {
        Map<String, Object> stats = adminService.getDashboardStats();

        if (stats.containsKey("error")) {
            Router.sendError(exchange, 500, (String) stats.get("error"));
            return;
        }

        String json = "{" +
            "\"totalUsers\":"         + stats.get("totalUsers")         + "," +
            "\"totalEvents\":"        + stats.get("totalEvents")        + "," +
            "\"totalRegistrations\":" + stats.get("totalRegistrations") + "," +
            "\"upcomingEvents\":"     + stats.get("upcomingEvents")     + "," +
            "\"topEvents\":"          + stats.get("topEventsJson")      + "," +
            "\"recentRegistrations\":" + stats.get("recentRegistrationsJson") + "," +
            "\"departmentStats\":"    + stats.get("departmentStatsJson") +
            "}";

        Router.sendJson(exchange, 200, JsonUtil.data(json));
    }

    // ─── GET /api/admin/users ─────────────────────────────────────────────────

    private void handleUsers(HttpExchange exchange, String method) throws IOException {
        if (!"GET".equals(method)) { Router.sendError(exchange, 405, "Method not allowed"); return; }
        try {
            List<User> users = adminService.getAllUsers();
            List<String> jsonList = new ArrayList<>();
            for (User u : users) jsonList.add(u.toJson());
            Router.sendJson(exchange, 200, JsonUtil.data(JsonUtil.listToJsonArray(jsonList)));
        } catch (Exception e) {
            Router.sendError(exchange, 500, e.getMessage());
        }
    }

    // ─── DELETE /api/admin/users/{id} ────────────────────────────────────────

    private void handleDeleteUser(HttpExchange exchange, String path) throws IOException {
        try {
            int userId = extractIdFromPath(path);
            boolean deleted = adminService.deleteUser(userId);
            if (!deleted) Router.sendError(exchange, 404, "User not found.");
            else          Router.sendJson(exchange, 200, JsonUtil.success("User deleted."));
        } catch (Exception e) {
            Router.sendError(exchange, 500, e.getMessage());
        }
    }

    // ─── GET /api/admin/registrations ────────────────────────────────────────

    private void handleRegistrations(HttpExchange exchange, Map<String, String> params) throws IOException {
        String search = params.get("search");
        String deptParam = params.get("department");
        String yearParam = params.get("year");
        Integer eventId  = params.containsKey("eventId") ? safeParseInt(params.get("eventId")) : null;
        Integer year     = yearParam != null ? safeParseInt(yearParam) : null;

        try {
            List<Registration> regs = regService.getAllRegistrations(search, eventId, deptParam, year);
            List<String> jsonList = new ArrayList<>();
            for (Registration r : regs) jsonList.add(r.toAdminJson());
            Router.sendJson(exchange, 200, JsonUtil.data(JsonUtil.listToJsonArray(jsonList)));
        } catch (Exception e) {
            Router.sendError(exchange, 500, e.getMessage());
        }
    }

    // ─── PUT /api/admin/registrations/{id}/cancel ─────────────────────────────

    private void handleAdminCancelRegistration(HttpExchange exchange, String path) throws IOException {
        try {
            // Extract ID from path like /api/admin/registrations/42/cancel
            String[] parts = path.split("/");
            int regId = Integer.parseInt(parts[parts.length - 2]);
            Map<String, Object> result = regService.adminCancelRegistration(regId);
            if (result.containsKey("error")) {
                Router.sendError(exchange, 404, (String) result.get("error"));
            } else {
                Router.sendJson(exchange, 200, JsonUtil.success("Registration cancelled."));
            }
        } catch (Exception e) {
            Router.sendError(exchange, 500, e.getMessage());
        }
    }

    // ─── Export endpoints ─────────────────────────────────────────────────────

    private void handleExportRegistrations(HttpExchange exchange, Map<String, String> params) throws IOException {
        int eventId = params.containsKey("eventId") ? safeParseInt(params.get("eventId")) : 0;
        String csv = CSVExporter.exportRegistrations(eventId);
        sendCsv(exchange, csv, "registrations.csv");
    }

    private void handleExportUsers(HttpExchange exchange) throws IOException {
        String csv = CSVExporter.exportUsers();
        sendCsv(exchange, csv, "users.csv");
    }

    private void handleExportEvents(HttpExchange exchange) throws IOException {
        String csv = CSVExporter.exportEvents();
        sendCsv(exchange, csv, "events.csv");
    }

    private void sendCsv(HttpExchange exchange, String csv, String filename) throws IOException {
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/csv; charset=utf-8");
        exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.getResponseBody().close();
    }

    // ─── Auth helper ──────────────────────────────────────────────────────────

    private boolean isAdmin(HttpExchange exchange) throws IOException {
        String token = Router.extractToken(exchange);
        SessionManager.SessionData session = authService.validateSession(token);
        if (session == null || !session.isAdmin()) {
            Router.sendError(exchange, 403, "Admin access required. Unauthorized.");
            return false;
        }
        return true;
    }

    // ─── Utilities ────────────────────────────────────────────────────────────

    private int extractIdFromPath(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }

    private int safeParseInt(String s) {
        try { return Integer.parseInt(s.trim()); }
        catch (Exception e) { return 0; }
    }
}
