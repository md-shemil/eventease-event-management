package com.eventease.model;

import java.time.LocalDateTime;

public class Bookmark {
    private Long id;
    private Long userId;
    private Long eventId;
    private LocalDateTime createdAt;

    public Bookmark() {
        this.createdAt = LocalDateTime.now();
    }

    public Bookmark(Long id, Long userId, Long eventId) {
        this();
        this.id = id;
        this.userId = userId;
        this.eventId = eventId;
    }

    public Bookmark(Long userId, Long eventId) {
        this(null, userId, eventId);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
