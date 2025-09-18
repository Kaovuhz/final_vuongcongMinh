package com.example.bookshop.service;

import com.example.bookshop.model.Book;
import com.example.bookshop.model.Category;
import com.example.bookshop.repository.BookRepository;
import com.example.bookshop.repository.CartRepository;
import com.example.bookshop.repository.FileStore;
import com.example.bookshop.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CartAndCheckoutServiceTest {
    private Path tempDir;
    private FileStore store;
    private BookRepository bookRepo;
    private CartRepository cartRepo;
    private OrderRepository orderRepo;
    private AuthSessionService auth;
    private CartService cartService;
    private CheckoutService checkoutService;

    @BeforeEach
    void setup() throws Exception {
        tempDir = Files.createTempDirectory("bookshop-test-");
        store = new FileStore(tempDir.toString());
        bookRepo = new BookRepository(store);
        cartRepo = new CartRepository(store);
        orderRepo = new OrderRepository(store);
        auth = new AuthSessionService(Duration.ofSeconds(1));
        cartService = new CartService(auth, bookRepo, cartRepo);
        checkoutService = new CheckoutService(auth, bookRepo, cartRepo, orderRepo);

        Category cat = new Category();
        cat.setId(1L);
        cat.setName("Tech");

        Book b1 = new Book(); b1.setId(1L); b1.setTitle("A"); b1.setPrice(10.0); b1.setStockQuantity(5); b1.setCategory(cat); b1.setStatus(Book.BookStatus.AVAILABLE);
        Book b2 = new Book(); b2.setId(2L); b2.setTitle("B"); b2.setPrice(20.0); b2.setStockQuantity(2); b2.setCategory(cat); b2.setStatus(Book.BookStatus.AVAILABLE);
        bookRepo.saveAll(List.of(b1, b2));
    }

    @Test
    void addToCart_capsToStock_and_requiresLogin() {
        String token = auth.login("alice");
        int added = cartService.addToCart(token, 1L, 10);
        assertEquals(5, added);
        assertThrows(IllegalStateException.class, () -> cartService.addToCart("expired", 1L, 1));
        assertThrows(IllegalStateException.class, () -> cartService.addToCart(null, 1L, 1));
    }

    @Test
    void sessionExpiry_requiresRelogin() throws Exception {
        String token = auth.login("bob");
        Thread.sleep(1100);
        assertTrue(auth.requireActiveUsername(token).isEmpty());
        assertThrows(IllegalStateException.class, () -> cartService.addToCart(token, 1L, 1));
        String token2 = auth.login("bob");
        int added = cartService.addToCart(token2, 2L, 3);
        assertEquals(2, added);
    }

    @Test
    void checkout_decreasesStock_and_clearsCart() {
        String token = auth.login("carol");
        cartService.addToCart(token, 1L, 3);
        cartService.addToCart(token, 2L, 2);
        double total = checkoutService.checkout(token);
        assertEquals(3 * 10.0 + 2 * 20.0, total, 0.0001);
        assertEquals(2, bookRepo.findById(1L).get().getStockQuantity());
        assertEquals(Book.BookStatus.AVAILABLE, bookRepo.findById(1L).get().getStatus());
        assertEquals(0, bookRepo.findById(2L).get().getStockQuantity());
        assertEquals(Book.BookStatus.OUT_OF_STOCK, bookRepo.findById(2L).get().getStatus());
        // Verify order statuses recorded
        var orders = orderRepo.findAll();
        assertEquals(2, orders.size());
        assertTrue(orders.stream().allMatch(o -> o.getStatus() == com.example.bookshop.model.Order.OrderStatus.COMPLETED));
    }

    @Test
    void cancel_clearsCart_noStockChange_and_cartNotPurchase() {
        String token = auth.login("dave");
        cartService.addToCart(token, 1L, 1);
        checkoutService.cancel(token);
        assertEquals(5, bookRepo.findById(1L).get().getStockQuantity());
        var orders = orderRepo.findAll();
        assertEquals(1, orders.size());
        assertEquals(com.example.bookshop.model.Order.OrderStatus.CANCELLED, orders.get(0).getStatus());
    }

    @Test
    void addToCart_updatesExistingItemQuantity_cappedByStock() {
        String token = auth.login("erin");
        int first = cartService.addToCart(token, 1L, 2);
        assertEquals(2, first);
        int second = cartService.addToCart(token, 1L, 10);
        assertEquals(5, second);
    }

    @Test
    void addToCart_zeroOrNegativeDesired_returnsZero() {
        String token = auth.login("frank");
        assertEquals(0, cartService.addToCart(token, 1L, 0));
        assertEquals(0, cartService.addToCart(token, 1L, -3));
    }

    @Test
    void addToCart_withZeroStock_returnsZero() {
        // set book 2 stock to zero and try adding
        var b2 = bookRepo.findById(2L).get();
        b2.setStockQuantity(0);
        b2.setStatus(Book.BookStatus.OUT_OF_STOCK);
        bookRepo.saveAll(List.of(bookRepo.findById(1L).get(), b2));
        String token = auth.login("gina");
        assertEquals(0, cartService.addToCart(token, 2L, 5));
    }

    @Test
    void addToCart_bookNotFound_throws() {
        String token = auth.login("hank");
        assertThrows(IllegalArgumentException.class, () -> cartService.addToCart(token, 999L, 1));
    }

    @Test
    void checkout_requiresLogin_and_cartMustExist() {
        assertThrows(IllegalStateException.class, () -> checkoutService.checkout("nope"));
        String token = auth.login("ivy");
        assertThrows(IllegalStateException.class, () -> checkoutService.checkout(token));
        assertThrows(IllegalStateException.class, () -> checkoutService.checkout(null));
    }

    @Test
    void sessionExpires_beforeCheckout_requiresRelogin() throws Exception {
        String token = auth.login("jane");
        cartService.addToCart(token, 1L, 1);
        Thread.sleep(1100);
        assertThrows(IllegalStateException.class, () -> checkoutService.checkout(token));
        String token2 = auth.login("jane");
        double total = checkoutService.checkout(token2);
        assertEquals(10.0, total, 0.0001);
    }

    @Test
    void cancel_requiresLogin_and_handlesMissingCart() {
        assertThrows(IllegalStateException.class, () -> checkoutService.cancel("nope"));
        assertThrows(IllegalStateException.class, () -> checkoutService.cancel(null));
        String token = auth.login("kate");
        // no cart created yet
        assertDoesNotThrow(() -> checkoutService.cancel(token));
    }

    @Test
    void checkout_capsQuantity_ifStockDecreasedAfterAdding() {
        String token = auth.login("liam");
        cartService.addToCart(token, 1L, 5); // matches current stock 5
        // simulate stock decreased to 2 before checkout
        var b1 = bookRepo.findById(1L).get();
        b1.setStockQuantity(2);
        b1.setStatus(Book.BookStatus.AVAILABLE);
        bookRepo.saveAll(List.of(b1, bookRepo.findById(2L).get()));
        double total = checkoutService.checkout(token);
        assertEquals(2 * 10.0, total, 0.0001);
        assertEquals(0, bookRepo.findById(1L).get().getStockQuantity());
        assertEquals(Book.BookStatus.OUT_OF_STOCK, bookRepo.findById(1L).get().getStatus());
    }

    @Test
    void checkout_throws_ifBookRemovedBetweenAddAndCheckout() {
        String token = auth.login("mike");
        cartService.addToCart(token, 1L, 1);
        // remove book 1 from repo
        var b2 = bookRepo.findById(2L).get();
        bookRepo.saveAll(List.of(b2));
        assertThrows(IllegalStateException.class, () -> checkoutService.checkout(token));
    }
}


