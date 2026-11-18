package com.eventease.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class EventUpdateRequest {

    @Size(max = 150, message = "Event name must not exceed 150 characters")
    private String name;

    private String description;

    @Size(max = 200, message = "Venue must not exceed 200 characters")
    private String venue;

    private LocalDate date;

    private String time;

    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    private Long categoryId;

    private String bannerTheme;

    private String bannerIcon;

    private String status;

    public EventUpdateRequest() {
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

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
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
}
