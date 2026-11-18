package com.eventease.repository;

import com.eventease.model.Notification;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository {
    Optional<Notification> findById(Long id);
    List<Notification> findByUserId(Long userId);
    Notification save(Notification notification);
    void deleteById(Long id);
    long countUnreadByUserId(Long userId);
}
