package com.example.bookshop.repository;

import com.example.bookshop.model.Book;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookRepository {
    private final FileStore store;
    private final String file = "books.json";

    public BookRepository(FileStore store) {
        this.store = store;
    }

    public synchronized List<Book> findAll() {
        Book[] arr = store.readJson(file, Book[].class, () -> new Book[0]);
        List<Book> list = new ArrayList<>();
        for (Book b : arr) list.add(b);
        return list;
    }

    public synchronized Optional<Book> findById(Long id) {
        return findAll().stream().filter(b -> id.equals(b.getId())).findFirst();
    }

    public synchronized void saveAll(List<Book> books) {
        store.writeJson(file, books);
    }
}


