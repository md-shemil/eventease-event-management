package com.eventease.model;

import java.time.LocalDateTime;

public class Registration {
    private Long id;
    private Long eventId;
    private Long userId;
    private String userName;
    private String userEmail;
    private LocalDateTime registrationDate;
    private String status; // CONFIRMED, CANCELLED

    public Registration() {
        this.registrationDate = LocalDateTime.now();
        this.status = "CONFIRMED";
    }

    public Registration(Long id, Long eventId, Long userId, String userName, String userEmail) {
        this();
        this.id = id;
        this.eventId = eventId;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
    }

    public Registration(Long eventId, Long userId, String userName, String userEmail) {
        this(null, eventId, userId, userName, userEmail);
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
