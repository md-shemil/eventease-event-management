package com.eventease.dto.response;

public class CategoryResponse {
    private Long id;
    private String name;
    private String description;
    private String icon;
    private long eventCount;

    public CategoryResponse() {
    }

    public CategoryResponse(Long id, String name, String description, String icon, long eventCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.eventCount = eventCount;
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

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public long getEventCount() {
        return eventCount;
    }

    public void setEventCount(long eventCount) {
        this.eventCount = eventCount;
    }
}
