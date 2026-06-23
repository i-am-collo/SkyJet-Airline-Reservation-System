package models;



/**
 * Manages user sessions and authentication tokens.
 * Provides session creation, validation, and cleanup.
 */
public class SessionManager {

    private static SessionManager instance;
    private static String currentSessionToken;
    private static long sessionExpiry;
    private static final long SESSION_DURATION = 60 * 60 * 1000; // 1 hour in milliseconds

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Create a new session with the JWT token from the backend.
     * @param user The authenticated user
     * @param token JWT token from the backend
     * @param expiresInSeconds Token expiry in seconds
     * @return Session token
     */
    public String createSession(User user, String token, long expiresInSeconds) {
        currentSessionToken = token;
        sessionExpiry = System.currentTimeMillis() + (expiresInSeconds * 1000);
        return currentSessionToken;
    }

    /**
     * Get current session token
     * @return Session token or null if no active session
     */
    public String getSessionToken() {
        if (isSessionValid()) {
            return currentSessionToken;
        }
        return null;
    }

    /**
     * Check if current session is valid
     * @return true if session exists and hasn't expired
     */
    public boolean isSessionValid() {
        return currentSessionToken != null && System.currentTimeMillis() < sessionExpiry;
    }

    /**
     * Invalidate current session (logout)
     */
    public void invalidateSession() {
        currentSessionToken = null;
        sessionExpiry = 0;
    }

    /**
     * Get remaining session time in seconds
     * @return Seconds remaining, or 0 if expired/no session
     */
    public long getSessionRemainingSeconds() {
        if (!isSessionValid()) return 0;
        return (sessionExpiry - System.currentTimeMillis()) / 1000;
    }
}
