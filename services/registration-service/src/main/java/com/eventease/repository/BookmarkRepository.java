package com.eventease.repository;

import com.eventease.model.Bookmark;
import java.util.List;
import java.util.Optional;

public interface BookmarkRepository {
    Optional<Bookmark> findById(Long id);
    List<Bookmark> findByUserId(Long userId);
    boolean existsByUserIdAndEventId(Long userId, Long eventId);
    Optional<Bookmark> findByUserIdAndEventId(Long userId, Long eventId);
    Bookmark save(Bookmark bookmark);
    void deleteById(Long id);
    void deleteByUserIdAndEventId(Long userId, Long eventId);
}
