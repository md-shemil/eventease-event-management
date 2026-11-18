package com.eventease.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class BookmarkResponse {
    private Long id;
    private Long eventId;
    private String eventName;
    private String eventVenue;
    private LocalDate eventDate;
    private String bannerTheme;
    private String bannerIcon;
    private LocalDateTime createdAt;

    public BookmarkResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventVenue() {
        return eventVenue;
    }

    public void setEventVenue(String eventVenue) {
        this.eventVenue = eventVenue;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
