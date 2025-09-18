package com.example.bookshop.repository;

import com.example.bookshop.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository {
    private final FileStore store;
    private final String file = "users.json";

    public UserRepository(FileStore store) { this.store = store; }

    public synchronized List<User> findAll() {
        User[] arr = store.readJson(file, User[].class, () -> new User[0]);
        List<User> list = new ArrayList<>();
        for (User u : arr) list.add(u);
        return list;
    }

    public synchronized Optional<User> findByUsername(String username) {
        return findAll().stream().filter(u -> username.equals(u.getUsername())).findFirst();
    }

    public synchronized Optional<User> findByUsernameAndPassword(String username, String password) {
        return findAll().stream().filter(u -> username.equals(u.getUsername()) && password.equals(u.getPassword())).findFirst();
    }

    public synchronized void save(User user) {
        List<User> all = findAll();
        all.removeIf(u -> u.getUsername().equals(user.getUsername()));
        all.add(user);
        store.writeJson(file, all);
    }
}


