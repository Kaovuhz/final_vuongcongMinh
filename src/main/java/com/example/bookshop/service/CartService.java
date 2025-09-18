package com.example.bookshop.service;

import com.example.bookshop.model.Book;
import com.example.bookshop.model.Cart;
import com.example.bookshop.model.CartItem;
import com.example.bookshop.repository.BookRepository;
import com.example.bookshop.repository.CartRepository;

import java.util.Optional;

public class CartService {
    private final AuthSessionService auth;
    private final BookRepository books;
    private final CartRepository carts;

    public CartService(AuthSessionService auth, BookRepository books, CartRepository carts) {
        this.auth = auth;
        this.books = books;
        this.carts = carts;
    }

    public int addToCart(String sessionToken, Long bookId, int desiredQuantity) {
        String username = auth.requireActiveUsername(sessionToken).orElseThrow(() -> new IllegalStateException("NOT_LOGGED_IN"));
        Book book = books.findById(bookId).orElseThrow(() -> new IllegalArgumentException("BOOK_NOT_FOUND"));
        int stock = book.getStockQuantity();
        int finalQty = Math.max(0, Math.min(desiredQuantity, stock));
        if (finalQty == 0) return 0;

        Cart cart = carts.findByUsername(username).orElseGet(() -> {
            Cart c = new Cart();
            c.setUsername(username);
            return c;
        });

        Optional<CartItem> existing = cart.getItems().stream().filter(i -> i.getBook().getId().equals(bookId)).findFirst();
        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(Math.min(finalQty, stock));
        } else {
            CartItem item = new CartItem();
            item.setId(System.nanoTime());
            item.setBook(book);
            item.setQuantity(finalQty);
            cart.getItems().add(item);
        }
        carts.save(cart);
        return finalQty;
    }
}


