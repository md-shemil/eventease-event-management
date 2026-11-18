package com.eventease.dto.response;

import java.util.List;

public class AdminDashboardResponse {
    private long totalEvents;
    private long totalUsers;
    private long totalRegistrations;
    private long upcomingEventsCount;
    private List<CategoryResponse> categoryDistribution;
    private List<RegistrationResponse> recentRegistrations;

    public AdminDashboardResponse() {
    }

    public long getTotalEvents() {
        return totalEvents;
    }

    public void setTotalEvents(long totalEvents) {
        this.totalEvents = totalEvents;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalRegistrations() {
        return totalRegistrations;
    }

    public void setTotalRegistrations(long totalRegistrations) {
        this.totalRegistrations = totalRegistrations;
    }

    public long getUpcomingEventsCount() {
        return upcomingEventsCount;
    }

    public void setUpcomingEventsCount(long upcomingEventsCount) {
        this.upcomingEventsCount = upcomingEventsCount;
    }

    public List<CategoryResponse> getCategoryDistribution() {
        return categoryDistribution;
    }

    public void setCategoryDistribution(List<CategoryResponse> categoryDistribution) {
        this.categoryDistribution = categoryDistribution;
    }

    public List<RegistrationResponse> getRecentRegistrations() {
        return recentRegistrations;
    }

    public void setRecentRegistrations(List<RegistrationResponse> recentRegistrations) {
        this.recentRegistrations = recentRegistrations;
    }
}
