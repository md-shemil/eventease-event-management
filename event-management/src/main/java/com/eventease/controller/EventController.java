package com.eventease.controller;

import com.eventease.dto.request.EventCreateRequest;
import com.eventease.dto.request.EventUpdateRequest;
import com.eventease.dto.request.RegistrationRequest;
import com.eventease.dto.response.BookmarkResponse;
import com.eventease.dto.response.EventResponse;
import com.eventease.dto.response.RegistrationResponse;
import com.eventease.security.UserPrincipal;
import com.eventease.service.BookmarkService;
import com.eventease.service.EventService;
import com.eventease.service.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;
    private final RegistrationService registrationService;
    private final BookmarkService bookmarkService;

    public EventController(EventService eventService,
                           RegistrationService registrationService,
                           BookmarkService bookmarkService) {
        this.eventService = eventService;
        this.registrationService = registrationService;
        this.bookmarkService = bookmarkService;
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> getAllEvents(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long currentUserId = principal != null ? principal.getId() : null;
        return ResponseEntity.ok(eventService.getAllEvents(categoryId, search, currentUserId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long currentUserId = principal != null ? principal.getId() : null;
        return ResponseEntity.ok(eventService.getEventById(id, currentUserId));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EventResponse> createEvent(
            @Valid @RequestBody EventCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long organizerId = principal != null ? principal.getId() : null;
        EventResponse created = eventService.createEvent(request, organizerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        boolean isAdmin = principal.getUser().getRole().name().equalsIgnoreCase("ADMIN");
        EventResponse updated = eventService.updateEvent(id, request, principal.getId(), isAdmin);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> deleteEvent(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        boolean isAdmin = principal.getUser().getRole().name().equalsIgnoreCase("ADMIN");
        eventService.deleteEvent(id, principal.getId(), isAdmin);
        return ResponseEntity.ok(Map.of("message", "Event deleted successfully"));
    }

    // Modern register endpoint: POST /api/events/{id}/register
    @PostMapping("/{id}/register")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RegistrationResponse> registerForEvent(
            @PathVariable Long id,
            @RequestBody(required = false) RegistrationRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        String name = principal.getName();
        String email = principal.getEmail();
        if (request != null && request.getName() != null && !request.getName().isBlank()) {
            name = request.getName();
        }
        if (request != null && request.getEmail() != null && !request.getEmail().isBlank()) {
            email = request.getEmail();
        }

        RegistrationResponse response = registrationService.registerParticipant(
                id, name, email, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Cancel registration: DELETE /api/events/{id}/register
    @DeleteMapping("/{id}/register")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> cancelRegistration(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        registrationService.cancelRegistration(id, principal.getId());
        return ResponseEntity.ok(Map.of("message", "Registration cancelled successfully"));
    }

    // Bookmark event: POST /api/events/{id}/bookmark
    @PostMapping("/{id}/bookmark")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookmarkResponse> bookmarkEvent(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        BookmarkResponse response = bookmarkService.addBookmark(principal.getId(), id);
        return ResponseEntity.ok(response);
    }

    // Remove bookmark: DELETE /api/events/{id}/bookmark
    @DeleteMapping("/{id}/bookmark")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> removeBookmark(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        bookmarkService.removeBookmark(principal.getId(), id);
        return ResponseEntity.ok(Map.of("message", "Bookmark removed successfully"));
    }

    // Legacy guest/user registrations support: POST /api/events/{eventId}/registrations
    @PostMapping("/{eventId}/registrations")
    public ResponseEntity<RegistrationResponse> registerParticipantLegacy(
            @PathVariable Long eventId,
            @RequestBody RegistrationRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal != null ? principal.getId() : null;
        String name = request != null && request.getName() != null ? request.getName() : (principal != null ? principal.getName() : "");
        String email = request != null && request.getEmail() != null ? request.getEmail() : (principal != null ? principal.getEmail() : "");

        RegistrationResponse response = registrationService.registerParticipant(
                eventId, name, email, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Legacy get registrations: GET /api/events/{eventId}/registrations
    @GetMapping("/{eventId}/registrations")
    public ResponseEntity<List<RegistrationResponse>> getEventRegistrations(
            @PathVariable Long eventId) {
        return ResponseEntity.ok(registrationService.getEventRegistrations(eventId));
    }
}
