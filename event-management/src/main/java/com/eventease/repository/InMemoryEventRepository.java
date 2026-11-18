package com.eventease.repository;

import com.eventease.model.Event;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InMemoryEventRepository implements EventRepository {

    private final ConcurrentHashMap<Long, Event> storage = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(0);

    @Override
    public Optional<Event> findById(Long id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Event> findAll() {
        return storage.values().stream()
                .sorted(Comparator.comparing(Event::getDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Event::getId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Event> findByCategoryId(Long categoryId) {
        if (categoryId == null) return findAll();
        return storage.values().stream()
                .filter(e -> categoryId.equals(e.getCategoryId()))
                .sorted(Comparator.comparing(Event::getDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    @Override
    public List<Event> search(String query) {
        if (query == null || query.isBlank()) {
            return findAll();
        }
        String lower = query.trim().toLowerCase();
        return storage.values().stream()
                .filter(e -> (e.getName() != null && e.getName().toLowerCase().contains(lower))
                        || (e.getVenue() != null && e.getVenue().toLowerCase().contains(lower))
                        || (e.getDescription() != null && e.getDescription().toLowerCase().contains(lower)))
                .sorted(Comparator.comparing(Event::getDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    @Override
    public Event save(Event event) {
        if (event.getId() == null) {
            event.setId(idSequence.incrementAndGet());
        }
        storage.put(event.getId(), event);
        return event;
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

    @Override
    public long count() {
        return storage.size();
    }
}
