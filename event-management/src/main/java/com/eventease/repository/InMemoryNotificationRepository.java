package com.eventease.repository;

import com.eventease.model.Notification;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InMemoryNotificationRepository implements NotificationRepository {

    private final ConcurrentHashMap<Long, Notification> storage = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(0);

    @Override
    public Optional<Notification> findById(Long id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Notification> findByUserId(Long userId) {
        if (userId == null) return new ArrayList<>();
        return storage.values().stream()
                .filter(n -> userId.equals(n.getUserId()))
                .sorted(Comparator.comparing(Notification::getCreatedAt, Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }

    @Override
    public Notification save(Notification notification) {
        if (notification.getId() == null) {
            notification.setId(idSequence.incrementAndGet());
        }
        storage.put(notification.getId(), notification);
        return notification;
    }

    @Override
    public void deleteById(Long id) {
        if (id != null) {
            storage.remove(id);
        }
    }

    @Override
    public long countUnreadByUserId(Long userId) {
        if (userId == null) return 0;
        return storage.values().stream()
                .filter(n -> userId.equals(n.getUserId()) && !n.isRead())
                .count();
    }
}
