package com.example.bookshop.service;

import com.example.bookshop.model.User;
import com.example.bookshop.repository.UserRepository;

import java.util.Objects;

public class UserService {
    private final UserRepository users;
    private final AuthSessionService auth;

    public UserService(UserRepository users, AuthSessionService auth) {
        this.users = users;
        this.auth = auth;
    }

    public void register(User user) {
        Objects.requireNonNull(user, "user");
        if (user.getUsername() == null || user.getPassword() == null) {
            throw new IllegalArgumentException("USERNAME_PASSWORD_REQUIRED");
        }
        if (users.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalStateException("USERNAME_EXISTS");
        }
        users.save(user);
    }

    public String login(String username, String password) {
        return users.findByUsernameAndPassword(username, password)
                .map(u -> auth.login(u.getUsername()))
                .orElseThrow(() -> new IllegalStateException("INVALID_CREDENTIALS"));
    }
}


