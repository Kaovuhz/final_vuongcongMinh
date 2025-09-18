package com.example.bookshop.service;

import com.example.bookshop.model.Book;
import com.example.bookshop.model.Cart;
import com.example.bookshop.model.CartItem;
import com.example.bookshop.repository.BookRepository;
import com.example.bookshop.repository.OrderRepository;
import com.example.bookshop.repository.CartRepository;

import java.util.List;

public class CheckoutService {
    private final AuthSessionService auth;
    private final BookRepository books;
    private final CartRepository carts;
    private final OrderRepository orders;

    public CheckoutService(AuthSessionService auth, BookRepository books, CartRepository carts, OrderRepository orders) {
        this.auth = auth;
        this.books = books;
        this.carts = carts;
        this.orders = orders;
    }

    public double checkout(String sessionToken) {
        String username = auth.requireActiveUsername(sessionToken).orElseThrow(() -> new IllegalStateException("NOT_LOGGED_IN"));
        Cart cart = carts.findByUsername(username).orElseThrow(() -> new IllegalStateException("CART_EMPTY"));
        List<Book> all = books.findAll();

        double total = 0.0;
        for (CartItem item : cart.getItems()) {
            Book book = all.stream().filter(b -> b.getId().equals(item.getBook().getId())).findFirst()
                .orElseThrow(() -> new IllegalStateException("BOOK_NOT_FOUND"));
            int purchasable = Math.min(item.getQuantity(), book.getStockQuantity());
            total += purchasable * book.getPrice();
            book.setStockQuantity(book.getStockQuantity() - purchasable);
            if (book.getStockQuantity() <= 0) {
                book.setStockQuantity(0);
                book.setStatus(Book.BookStatus.OUT_OF_STOCK);
            } else {
                book.setStatus(Book.BookStatus.AVAILABLE);
            }
            com.example.bookshop.model.Order o = new com.example.bookshop.model.Order();
            o.setId(System.nanoTime());
            o.setBook(book);
            o.setQuantity(purchasable);
            o.setStatus(com.example.bookshop.model.Order.OrderStatus.COMPLETED);
            orders.save(o);
        }
        books.saveAll(all);
        cart.getItems().clear();
        carts.save(cart);
        return total;
    }

    public void cancel(String sessionToken) {
        String username = auth.requireActiveUsername(sessionToken).orElseThrow(() -> new IllegalStateException("NOT_LOGGED_IN"));
        carts.findByUsername(username).ifPresent(c -> {
            // record cancelled orders for items in cart, quantities as requested
            for (CartItem item : c.getItems()) {
                com.example.bookshop.model.Order o = new com.example.bookshop.model.Order();
                o.setId(System.nanoTime());
                o.setBook(item.getBook());
                o.setQuantity(item.getQuantity());
                o.setStatus(com.example.bookshop.model.Order.OrderStatus.CANCELLED);
                orders.save(o);
            }
            c.getItems().clear();
            carts.save(c);
        });
    }
}


