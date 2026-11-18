package com.eventease.repository;

import com.eventease.model.Bookmark;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InMemoryBookmarkRepository implements BookmarkRepository {

    private final ConcurrentHashMap<Long, Bookmark> storage = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(0);

    @Override
    public Optional<Bookmark> findById(Long id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Bookmark> findByUserId(Long userId) {
        if (userId == null) return new ArrayList<>();
        return storage.values().stream()
                .filter(b -> userId.equals(b.getUserId()))
                .sorted(Comparator.comparing(Bookmark::getCreatedAt, Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByUserIdAndEventId(Long userId, Long eventId) {
        if (userId == null || eventId == null) return false;
        return storage.values().stream()
                .anyMatch(b -> userId.equals(b.getUserId()) && eventId.equals(b.getEventId()));
    }

    @Override
    public Optional<Bookmark> findByUserIdAndEventId(Long userId, Long eventId) {
        if (userId == null || eventId == null) return Optional.empty();
        return storage.values().stream()
                .filter(b -> userId.equals(b.getUserId()) && eventId.equals(b.getEventId()))
                .findFirst();
    }

    @Override
    public Bookmark save(Bookmark bookmark) {
        if (bookmark.getId() == null) {
            bookmark.setId(idSequence.incrementAndGet());
        }
        storage.put(bookmark.getId(), bookmark);
        return bookmark;
    }

    @Override
    public void deleteById(Long id) {
        if (id != null) {
            storage.remove(id);
        }
    }

    @Override
    public void deleteByUserIdAndEventId(Long userId, Long eventId) {
        if (userId == null || eventId == null) return;
        storage.values().removeIf(b -> userId.equals(b.getUserId()) && eventId.equals(b.getEventId()));
    }
}
