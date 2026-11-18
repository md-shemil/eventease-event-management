package com.eventease.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Event {
    private Long id;
    private String name;
    private String description;
    private String venue;
    private LocalDate date;
    private String time;
    private int capacity;
    private int registeredCount;
    private Long categoryId;
    private Long organizerId;
    private String bannerTheme; // e.g. "purple", "pink", "blue", "yellow", "green", "orange"
    private String bannerIcon;  // e.g. "✦", "🎵", "🚀", "🎨", "🎓", "🎤"
    private String status;      // UPCOMING, FULL, COMPLETED, CANCELLED
    private LocalDateTime createdAt;

    public Event() {
        this.status = "UPCOMING";
        this.createdAt = LocalDateTime.now();
        this.registeredCount = 0;
        this.bannerTheme = "purple";
        this.bannerIcon = "✦";
        this.time = "10:00 AM";
    }

    public Event(String name, String venue, LocalDate date, int capacity) {
        this();
        this.name = name;
        this.venue = venue;
        this.date = date;
        this.capacity = capacity;
    }

    public Event(Long id, String name, String description, String venue,
                 LocalDate date, String time, int capacity, Long categoryId,
                 Long organizerId, String bannerTheme, String bannerIcon) {
        this();
        this.id = id;
        this.name = name;
        this.description = description;
        this.venue = venue;
        this.date = date;
        this.time = time != null ? time : "10:00 AM";
        this.capacity = capacity;
        this.categoryId = categoryId;
        this.organizerId = organizerId;
        if (bannerTheme != null) this.bannerTheme = bannerTheme;
        if (bannerIcon != null) this.bannerIcon = bannerIcon;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getRegisteredCount() {
        return registeredCount;
    }

    public void setRegisteredCount(int registeredCount) {
        this.registeredCount = registeredCount;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(Long organizerId) {
        this.organizerId = organizerId;
    }

    public String getBannerTheme() {
        return bannerTheme;
    }

    public void setBannerTheme(String bannerTheme) {
        this.bannerTheme = bannerTheme;
    }

    public String getBannerIcon() {
        return bannerIcon;
    }

    public void setBannerIcon(String bannerIcon) {
        this.bannerIcon = bannerIcon;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getAvailableCapacity() {
        return Math.max(0, capacity - registeredCount);
    }
}
