package com.eventease.controller;

import com.eventease.dto.request.EventCreateRequest;
import com.eventease.dto.request.EventUpdateRequest;
import com.eventease.dto.response.EventResponse;
import com.eventease.model.Event;
import com.eventease.security.UserPrincipal;
import com.eventease.service.EventService;
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
@CrossOrigin(origins = "*", maxAge = 3600)
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> getAllEvents(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal != null ? principal.getId() : null;
        return ResponseEntity.ok(eventService.getAllEvents(categoryId, search, userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal != null ? principal.getId() : null;
        return ResponseEntity.ok(eventService.getEventById(id, userId));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EventResponse> createEvent(
            @Valid @RequestBody EventCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        EventResponse created = eventService.createEvent(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(eventService.updateEvent(id, request, principal.getId(), isAdmin));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> deleteEvent(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        eventService.deleteEvent(id, principal.getId(), isAdmin);
        return ResponseEntity.ok(Map.of("message", "Event deleted successfully"));
    }

    // Internal capacity management endpoints for registration-service
    @GetMapping("/{id}/entity")
    public ResponseEntity<Event> getEventEntity(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEventEntity(id));
    }

    @PostMapping("/{id}/increment")
    public ResponseEntity<Event> incrementCount(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.incrementRegisteredCount(id));
    }

    @PostMapping("/{id}/decrement")
    public ResponseEntity<Event> decrementCount(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.decrementRegisteredCount(id));
    }
}
