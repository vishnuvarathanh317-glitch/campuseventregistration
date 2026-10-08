import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.util.*;

/**
 * RegistrationController — Handles /api/events/{id}/register and /api/registrations/*
 */
public class RegistrationController {

    private final RegistrationService regService  = new RegistrationService();
    private final AuthenticationService authService = new AuthenticationService();

    // ─── POST /api/events/{eventId}/register ──────────────────────────────────

    public void handleRegister(HttpExchange exchange, int eventId) throws IOException {
        SessionManager.SessionData session = requireAuth(exchange);
        if (session == null) return;

        Map<String, Object> result = regService.register(session.userId, eventId);

        if (result.containsKey("error")) {
            int code = Boolean.TRUE.equals(result.get("duplicate")) ? 409 :
                       Boolean.TRUE.equals(result.get("full"))      ? 409 : 400;
            Router.sendJson(exchange, code, JsonUtil.error((String) result.get("error")));
        } else {
            Map<String, Object> data = new HashMap<>();
            data.put("registrationId", result.get("registrationId"));
            data.put("eventTitle",     result.get("eventTitle"));
            data.put("eventDate",      result.get("eventDate"));
            data.put("studentName",    result.get("studentName"));
            Router.sendJson(exchange, 201, JsonUtil.data(JsonUtil.mapToJson(data)));
        }
    }

    // ─── GET /api/registrations/my ───────────────────────────────────────────

    public void handleMyRegistrations(HttpExchange exchange) throws IOException {
        SessionManager.SessionData session = requireAuth(exchange);
        if (session == null) return;

        try {
            List<Registration> regs = regService.getMyRegistrations(session.userId);
            List<String> jsonList = new ArrayList<>();
            for (Registration r : regs) jsonList.add(r.toStudentJson());
            Router.sendJson(exchange, 200, JsonUtil.data(JsonUtil.listToJsonArray(jsonList)));
        } catch (Exception e) {
            Router.sendError(exchange, 500, e.getMessage());
        }
    }

    // ─── DELETE /api/registrations/{id} ──────────────────────────────────────

    public void handleCancel(HttpExchange exchange, int registrationId) throws IOException {
        SessionManager.SessionData session = requireAuth(exchange);
        if (session == null) return;

        Map<String, Object> result = regService.cancelRegistration(registrationId, session.userId);

        if (result.containsKey("error")) {
            Router.sendJson(exchange, 403, JsonUtil.error((String) result.get("error")));
        } else {
            Router.sendJson(exchange, 200, JsonUtil.success("Registration cancelled."));
        }
    }

    // ─── Auth helper ──────────────────────────────────────────────────────────

    private SessionManager.SessionData requireAuth(HttpExchange exchange) throws IOException {
        String token = Router.extractToken(exchange);
        SessionManager.SessionData session = authService.validateSession(token);
        if (session == null) {
            Router.sendError(exchange, 401, "Authentication required. Please log in.");
            return null;
        }
        return session;
    }
}
