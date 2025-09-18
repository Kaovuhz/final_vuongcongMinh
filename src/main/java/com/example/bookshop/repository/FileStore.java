package com.example.bookshop.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;

public class FileStore {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path baseDir;

    public FileStore(String baseDir) {
        this.baseDir = Paths.get(baseDir);
    }

    public synchronized <T> T readJson(String relativePath, Class<T> type, Supplier<T> defaultSupplier) {
        try {
            Path path = baseDir.resolve(relativePath);
            if (!Files.exists(path)) {
                T def = defaultSupplier.get();
                writeJson(relativePath, def);
                return def;
            }
            try (Reader r = Files.newBufferedReader(path)) {
                T data = gson.fromJson(r, type);
                if (data == null) return defaultSupplier.get();
                return data;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void writeJson(String relativePath, Object data) {
        try {
            Path path = baseDir.resolve(relativePath);
            Files.createDirectories(path.getParent());
            try (Writer w = Files.newBufferedWriter(path)) {
                gson.toJson(data, w);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}


