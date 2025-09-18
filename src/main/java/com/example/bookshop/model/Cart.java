package com.example.bookshop.model;

import java.util.ArrayList;
import java.util.List;

public class Cart {
    private Long id;
    private String username; // link to User by username
    private List<CartItem> items = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }
}


