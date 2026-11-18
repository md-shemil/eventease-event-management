package com.eventease.repository;

import com.eventease.model.Event;
import java.util.List;
import java.util.Optional;

public interface EventRepository {
    Optional<Event> findById(Long id);
    List<Event> findAll();
    List<Event> findByCategoryId(Long categoryId);
    List<Event> search(String query);
    Event save(Event event);
    void deleteById(Long id);
    boolean existsById(Long id);
    long count();
}
