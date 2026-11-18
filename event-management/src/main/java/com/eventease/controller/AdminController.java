package com.eventease.controller;

import com.eventease.dto.response.AdminDashboardResponse;
import com.eventease.dto.response.EventResponse;
import com.eventease.dto.response.RegistrationResponse;
import com.eventease.dto.response.UserSummaryResponse;
import com.eventease.service.CategoryService;
import com.eventease.service.EventService;
import com.eventease.service.RegistrationService;
import com.eventease.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final EventService eventService;
    private final UserService userService;
    private final RegistrationService registrationService;
    private final CategoryService categoryService;

    public AdminController(EventService eventService,
                           UserService userService,
                           RegistrationService registrationService,
                           CategoryService categoryService) {
        this.eventService = eventService;
        this.userService = userService;
        this.registrationService = registrationService;
        this.categoryService = categoryService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> getDashboard() {
        AdminDashboardResponse response = new AdminDashboardResponse();
        response.setTotalEvents(eventService.getTotalEventCount());
        response.setTotalUsers(userService.getUserCount());
        response.setTotalRegistrations(registrationService.getTotalRegistrationCount());
        response.setUpcomingEventsCount(eventService.getUpcomingEventCount());
        response.setCategoryDistribution(categoryService.getAllCategories());

        List<RegistrationResponse> allRegistrations = registrationService.getAllRegistrations();
        response.setRecentRegistrations(
                allRegistrations.stream().limit(10).collect(Collectors.toList())
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/events")
    public ResponseEntity<List<EventResponse>> getAdminEvents() {
        return ResponseEntity.ok(eventService.getAdminEvents());
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserSummaryResponse>> getAdminUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
}
