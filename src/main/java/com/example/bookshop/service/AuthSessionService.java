package com.example.bookshop.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AuthSessionService {
    public static class SessionInfo {
        private final String username;
        private volatile Instant expiresAt;
        SessionInfo(String username, Instant expiresAt) { this.username = username; this.expiresAt = expiresAt; }
        public String getUsername() { return username; }
        public Instant getExpiresAt() { return expiresAt; }
        void extend(Duration ttl) { this.expiresAt = Instant.now().plus(ttl); }
    }

    private final Map<String, SessionInfo> sessions = new ConcurrentHashMap<>();
    private final Duration ttl;

    public AuthSessionService(Duration ttl) { this.ttl = ttl; }

    public String login(String username) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, new SessionInfo(username, Instant.now().plus(ttl)));
        return token;
    }

    public Optional<String> requireActiveUsername(String token) {
        if (token == null) return Optional.empty();
        SessionInfo info = sessions.get(token);
        if (info == null) return Optional.empty();
        if (Instant.now().isAfter(info.getExpiresAt())) {
            sessions.remove(token);
            return Optional.empty();
        }
        return Optional.of(info.getUsername());
    }

    public void logout(String token) { if (token != null) sessions.remove(token); }

    public void refresh(String token) {
        if (token == null) return;
        SessionInfo info = sessions.get(token);
        if (info != null) {
            info.extend(ttl);
        }
    }
}


