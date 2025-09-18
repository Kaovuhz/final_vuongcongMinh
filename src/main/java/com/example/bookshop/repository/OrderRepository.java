package com.example.bookshop.repository;

import com.example.bookshop.model.Order;

import java.util.ArrayList;
import java.util.List;

public class OrderRepository {
    private final FileStore store;
    private final String file = "orders.json";

    public OrderRepository(FileStore store) { this.store = store; }

    public synchronized List<Order> findAll() {
        Order[] arr = store.readJson(file, Order[].class, () -> new Order[0]);
        List<Order> list = new ArrayList<>();
        for (Order o : arr) list.add(o);
        return list;
    }

    public synchronized void saveAll(List<Order> orders) {
        store.writeJson(file, orders);
    }

    public synchronized void save(Order order) {
        List<Order> all = findAll();
        all.add(order);
        saveAll(all);
    }
}


