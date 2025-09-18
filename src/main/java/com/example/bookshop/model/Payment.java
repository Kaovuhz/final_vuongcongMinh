package com.example.bookshop.model;

public class Payment {
    public enum PaymentMethod { COD, CREDIT_CARD }
    public enum PaymentStatus { PENDING, SUCCESS, FAILED, CANCELLED }

    private Long id;
    private Order order;
    private PaymentMethod method;
    private double amount;
    private PaymentStatus status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    public PaymentMethod getMethod() { return method; }
    public void setMethod(PaymentMethod method) { this.method = method; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
}


