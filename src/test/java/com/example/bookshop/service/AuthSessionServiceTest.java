package com.example.bookshop.service;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class AuthSessionServiceTest {

    @Test
    void login_returnsToken_and_requireActiveUsername_ok() {
        AuthSessionService auth = new AuthSessionService(Duration.ofSeconds(5));
        String token = auth.login("alice");
        assertNotNull(token);
        assertEquals("alice", auth.requireActiveUsername(token).orElse(null));
    }

    @Test
    void logout_invalidatesSession() {
        AuthSessionService auth = new AuthSessionService(Duration.ofSeconds(5));
        String token = auth.login("bob");
        auth.logout(token);
        assertTrue(auth.requireActiveUsername(token).isEmpty());
    }

    @Test
    void expired_session_requiresRelogin() throws Exception {
        AuthSessionService auth = new AuthSessionService(Duration.ofMillis(200));
        String token = auth.login("carol");
        Thread.sleep(250);
        assertTrue(auth.requireActiveUsername(token).isEmpty());
        String token2 = auth.login("carol");
        assertNotEquals(token, token2);
        assertEquals("carol", auth.requireActiveUsername(token2).orElse(null));
    }

    @Test
    void different_users_have_independent_tokens() {
        AuthSessionService auth = new AuthSessionService(Duration.ofSeconds(5));
        String t1 = auth.login("dave");
        String t2 = auth.login("erin");
        assertEquals("dave", auth.requireActiveUsername(t1).orElse(null));
        assertEquals("erin", auth.requireActiveUsername(t2).orElse(null));
    }

    @Test
    void requireActiveUsername_nullToken_returnsEmpty() {
        AuthSessionService auth = new AuthSessionService(Duration.ofSeconds(5));
        assertTrue(auth.requireActiveUsername(null).isEmpty());
    }

    @Test
    void requireActiveUsername_unknownToken_returnsEmpty() {
        AuthSessionService auth = new AuthSessionService(Duration.ofSeconds(5));
        assertTrue(auth.requireActiveUsername("does-not-exist").isEmpty());
    }

    @Test
    void logout_null_noop_and_logout_unknown_noop() {
        AuthSessionService auth = new AuthSessionService(Duration.ofSeconds(5));
        // should not throw
        auth.logout(null);
        auth.logout("does-not-exist");
    }

    @Test
    void refresh_extends_session_expiry() throws Exception {
        AuthSessionService auth = new AuthSessionService(Duration.ofMillis(200));
        String token = auth.login("zoe");
        Thread.sleep(150);
        auth.refresh(token); // extend TTL by 200ms
        // Should still be valid after initial TTL would have expired
        Thread.sleep(100);
        assertEquals("zoe", auth.requireActiveUsername(token).orElse(null));
    }

    @Test
    void logout_removes_existing_token() {
        AuthSessionService auth = new AuthSessionService(Duration.ofSeconds(5));
        String token = auth.login("kate");
        assertEquals("kate", auth.requireActiveUsername(token).orElse(null));
        auth.logout(token);
        assertTrue(auth.requireActiveUsername(token).isEmpty());
    }

    @Test
    void refresh_null_and_unknown_token_are_noop() {
        AuthSessionService auth = new AuthSessionService(Duration.ofSeconds(5));
        String token = auth.login("lee");
        // null path
        auth.refresh(null);
        assertEquals("lee", auth.requireActiveUsername(token).orElse(null));
        // unknown token path (info == null)
        auth.refresh("not-found");
        assertEquals("lee", auth.requireActiveUsername(token).orElse(null));
    }
}


