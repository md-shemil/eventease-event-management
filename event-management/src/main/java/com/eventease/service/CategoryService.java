package com.eventease.service;

import com.eventease.dto.response.CategoryResponse;
import com.eventease.exception.ResourceNotFoundException;
import com.eventease.model.Category;
import com.eventease.repository.CategoryRepository;
import com.eventease.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    public CategoryService(CategoryRepository categoryRepository,
                           EventRepository eventRepository) {
        this.categoryRepository = categoryRepository;
        this.eventRepository = eventRepository;
    }

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        return mapToResponse(category);
    }

    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    private CategoryResponse mapToResponse(Category category) {
        long eventCount = eventRepository.findByCategoryId(category.getId()).size();
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getIcon(),
                eventCount
        );
    }
}
