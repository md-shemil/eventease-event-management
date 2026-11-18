package com.eventease.controller;

import com.eventease.dto.response.AdminDashboardResponse;
import com.eventease.dto.response.EventResponse;
import com.eventease.service.EventService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AdminController {

    private final EventService eventService;

    public AdminController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> getDashboardStats() {
        AdminDashboardResponse stats = new AdminDashboardResponse();
        stats.setTotalEvents(eventService.getTotalEventCount());
        stats.setUpcomingEventsCount(eventService.getUpcomingEventCount());
        stats.setTotalRegistrations(0L); // aggregated
        stats.setTotalUsers(0L);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/events")
    public ResponseEntity<List<EventResponse>> getAdminEvents() {
        return ResponseEntity.ok(eventService.getAdminEvents());
    }
}
