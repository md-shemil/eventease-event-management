package com.eventease.repository;

import com.eventease.model.Category;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InMemoryCategoryRepository implements CategoryRepository {

    private final ConcurrentHashMap<Long, Category> storage = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(0);

    @Override
    public Optional<Category> findById(Long id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Optional<Category> findByName(String name) {
        if (name == null) return Optional.empty();
        return storage.values().stream()
                .filter(c -> name.equalsIgnoreCase(c.getName()))
                .findFirst();
    }

    @Override
    public List<Category> findAll() {
        return storage.values().stream()
                .sorted(Comparator.comparing(Category::getId))
                .collect(Collectors.toList());
    }

    @Override
    public Category save(Category category) {
        if (category.getId() == null) {
            category.setId(idSequence.incrementAndGet());
        }
        storage.put(category.getId(), category);
        return category;
    }

    @Override
    public void deleteById(Long id) {
        if (id != null) {
            storage.remove(id);
        }
    }

    @Override
    public boolean existsById(Long id) {
        return id != null && storage.containsKey(id);
    }
}
