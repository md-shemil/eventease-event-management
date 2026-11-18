package com.eventease.repository;

import com.eventease.model.Category;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    Optional<Category> findById(Long id);
    Optional<Category> findByName(String name);
    List<Category> findAll();
    Category save(Category category);
    void deleteById(Long id);
    boolean existsById(Long id);
}
