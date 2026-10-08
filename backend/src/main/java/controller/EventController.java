import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.util.*;

/**
 * EventController — Handles /api/events/* endpoints.
 */
public class EventController {

    private final EventService        eventService = new EventService();
    private final AuthenticationService authService = new AuthenticationService();

    public void handle(HttpExchange exchange, String path, String method,
                       String pathParam) throws IOException {

        // Route: GET /api/events
        if (method.equals("GET") && pathParam == null) {
            handleList(exchange);
            return;
        }
        // Route: GET /api/events/{id}
        if (method.equals("GET") && pathParam != null) {
            handleGetOne(exchange, pathParam);
            return;
        }
        // Route: POST /api/events (admin only)
        if (method.equals("POST") && pathParam == null) {
            handleCreate(exchange);
            return;
        }
        // Route: PUT /api/events/{id} (admin only)
        if (method.equals("PUT") && pathParam != null) {
            handleUpdate(exchange, pathParam);
            return;
        }
        // Route: DELETE /api/events/{id} (admin only)
        if (method.equals("DELETE") && pathParam != null) {
            handleDelete(exchange, pathParam);
            return;
        }

        Router.sendError(exchange, 404, "Event endpoint not found");
    }

    // ─── GET /api/events ──────────────────────────────────────────────────────

    private void handleList(HttpExchange exchange) throws IOException {
        Map<String, String> params = Router.parseQuery(exchange.getRequestURI().getQuery());

        String search   = params.get("search");
        String status   = params.get("status");
        String category = params.get("category");
        String sortBy   = params.getOrDefault("sortBy", "date");
        boolean asc     = !"false".equalsIgnoreCase(params.get("asc"));

        try {
            List<Event> events = eventService.getEvents(search, status, category, sortBy, asc);
            List<String> jsonList = new ArrayList<>();
            for (Event e : events) jsonList.add(e.toSummaryJson());
            Router.sendJson(exchange, 200, JsonUtil.data(JsonUtil.listToJsonArray(jsonList)));
        } catch (Exception e) {
            Router.sendError(exchange, 500, "Failed to fetch events: " + e.getMessage());
        }
    }

    // ─── GET /api/events/{id} ────────────────────────────────────────────────

    private void handleGetOne(HttpExchange exchange, String idStr) throws IOException {
        try {
            int id = Integer.parseInt(idStr);
            Event event = eventService.getEventById(id);
            if (event == null) {
                Router.sendError(exchange, 404, "Event not found.");
            } else {
                Router.sendJson(exchange, 200, JsonUtil.data(event.toJson()));
            }
        } catch (NumberFormatException e) {
            Router.sendError(exchange, 400, "Invalid event ID.");
        } catch (Exception e) {
            Router.sendError(exchange, 500, e.getMessage());
        }
    }

    // ─── POST /api/events (admin only) ────────────────────────────────────────

    private void handleCreate(HttpExchange exchange) throws IOException {
        if (!isAdmin(exchange)) return;
        String body = Router.readBody(exchange);
        Map<String, Object> result = eventService.createEvent(body);
        if (result.containsKey("error")) {
            Router.sendJson(exchange, 400, JsonUtil.error((String) result.get("error")));
        } else {
            Router.sendJson(exchange, 201, JsonUtil.data((String) result.get("event")));
        }
    }

    // ─── PUT /api/events/{id} (admin only) ────────────────────────────────────

    private void handleUpdate(HttpExchange exchange, String idStr) throws IOException {
        if (!isAdmin(exchange)) return;
        try {
            int id = Integer.parseInt(idStr);
            String body = Router.readBody(exchange);
            Map<String, Object> result = eventService.updateEvent(id, body);
            if (result.containsKey("error")) {
                Router.sendJson(exchange, 400, JsonUtil.error((String) result.get("error")));
            } else {
                Router.sendJson(exchange, 200, JsonUtil.data((String) result.get("event")));
            }
        } catch (NumberFormatException e) {
            Router.sendError(exchange, 400, "Invalid event ID.");
        }
    }

    // ─── DELETE /api/events/{id} (admin only) ─────────────────────────────────

    private void handleDelete(HttpExchange exchange, String idStr) throws IOException {
        if (!isAdmin(exchange)) return;
        try {
            int id = Integer.parseInt(idStr);
            Map<String, Object> result = eventService.deleteEvent(id);
            if (result.containsKey("error")) {
                Router.sendJson(exchange, 404, JsonUtil.error((String) result.get("error")));
            } else {
                Router.sendJson(exchange, 200, JsonUtil.success("Event deleted successfully."));
            }
        } catch (NumberFormatException e) {
            Router.sendError(exchange, 400, "Invalid event ID.");
        }
    }

    // ─── Auth helper ──────────────────────────────────────────────────────────

    private boolean isAdmin(HttpExchange exchange) throws IOException {
        String token = Router.extractToken(exchange);
        SessionManager.SessionData session = authService.validateSession(token);
        if (session == null || !session.isAdmin()) {
            Router.sendError(exchange, 403, "Admin access required.");
            return false;
        }
        return true;
    }
}
