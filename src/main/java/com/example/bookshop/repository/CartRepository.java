package com.example.bookshop.repository;

import com.example.bookshop.model.Cart;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CartRepository {
    private final FileStore store;
    private final String file = "carts.json";

    public CartRepository(FileStore store) { this.store = store; }

    public synchronized List<Cart> findAll() {
        Cart[] arr = store.readJson(file, Cart[].class, () -> new Cart[0]);
        List<Cart> list = new ArrayList<>();
        for (Cart c : arr) list.add(c);
        return list;
    }

    public synchronized Optional<Cart> findByUsername(String username) {
        return findAll().stream().filter(c -> username.equals(c.getUsername())).findFirst();
    }

    public synchronized void save(Cart cart) {
        List<Cart> all = findAll();
        Optional<Cart> existing = all.stream().filter(c -> cart.getUsername().equals(c.getUsername())).findFirst();
        if (existing.isPresent()) {
            all.remove(existing.get());
        }
        all.add(cart);
        store.writeJson(file, all);
    }
}


