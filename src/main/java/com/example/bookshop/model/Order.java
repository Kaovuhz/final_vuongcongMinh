package com.example.bookshop.model;

public class Order {
    public enum OrderStatus { PENDING, COMPLETED, CANCELLED }
    private Long id;
    private Book book;
    private int quantity;
    private OrderStatus status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
}


