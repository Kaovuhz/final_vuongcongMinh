package com.example.bookshop.service;

import com.example.bookshop.model.User;
import com.example.bookshop.repository.FileStore;
import com.example.bookshop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {
    private Path tempDir;
    private FileStore store;
    private UserRepository userRepo;
    private AuthSessionService auth;
    private UserService userService;

    @BeforeEach
    void setup() throws Exception {
        tempDir = Files.createTempDirectory("user-test-");
        store = new FileStore(tempDir.toString());
        userRepo = new UserRepository(store);
        auth = new AuthSessionService(Duration.ofSeconds(5));
        userService = new UserService(userRepo, auth);
    }

    @Test
    void register_then_login_success() {
        User u = new User();
        u.setUsername("newuser");
        u.setPassword("pass");
        userService.register(u);
        String token = userService.login("newuser", "pass");
        assertNotNull(token);
        assertEquals("newuser", auth.requireActiveUsername(token).orElse(null));
    }

    @Test
    void register_duplicate_username_fails() {
        User u = new User();
        u.setUsername("dup");
        u.setPassword("p1");
        userService.register(u);
        User u2 = new User();
        u2.setUsername("dup");
        u2.setPassword("p2");
        assertThrows(IllegalStateException.class, () -> userService.register(u2));
    }

    @Test
    void login_wrong_password_fails() {
        User u = new User();
        u.setUsername("a");
        u.setPassword("b");
        userService.register(u);
        assertThrows(IllegalStateException.class, () -> userService.login("a", "wrong"));
    }
}


