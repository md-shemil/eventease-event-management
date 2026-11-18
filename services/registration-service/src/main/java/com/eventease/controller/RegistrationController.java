package com.eventease.controller;

import com.eventease.dto.request.RegistrationRequest;
import com.eventease.dto.response.BookmarkResponse;
import com.eventease.dto.response.RegistrationResponse;
import com.eventease.security.UserPrincipal;
import com.eventease.service.BookmarkService;
import com.eventease.service.RegistrationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", maxAge = 3600)
public class RegistrationController {

    private final RegistrationService registrationService;
    private final BookmarkService bookmarkService;

    public RegistrationController(RegistrationService registrationService,
                                  BookmarkService bookmarkService) {
        this.registrationService = registrationService;
        this.bookmarkService = bookmarkService;
    }

    // Authenticated user registration: POST /api/events/{id}/register
    @PostMapping("/events/{id}/register")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RegistrationResponse> registerForEvent(
            @PathVariable Long id,
            @RequestBody(required = false) RegistrationRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        String name = (request != null && request.getName() != null && !request.getName().isBlank())
                ? request.getName() : principal.getName();
        String email = (request != null && request.getEmail() != null && !request.getEmail().isBlank())
                ? request.getEmail() : principal.getUsername();

        RegistrationResponse response = registrationService.registerParticipant(id, name, email, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Legacy guest/user registrations support: POST /api/events/{id}/registrations
    @PostMapping("/events/{id}/registrations")
    public ResponseEntity<RegistrationResponse> registerParticipantLegacy(
            @PathVariable Long id,
            @RequestBody(required = false) RegistrationRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal != null ? principal.getId() : null;
        String name = request != null && request.getName() != null ? request.getName() : (principal != null ? principal.getName() : "");
        String email = request != null && request.getEmail() != null ? request.getEmail() : (principal != null ? principal.getUsername() : "");

        RegistrationResponse response = registrationService.registerParticipant(id, name, email, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Cancel registration: DELETE /api/events/{id}/register
    @DeleteMapping("/events/{id}/register")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> cancelRegistration(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        registrationService.cancelRegistration(id, principal.getId());
        return ResponseEntity.ok(Map.of("message", "Registration cancelled successfully"));
    }

    // Get event registrations: GET /api/events/{id}/registrations
    @GetMapping("/events/{id}/registrations")
    public ResponseEntity<List<RegistrationResponse>> getEventRegistrations(@PathVariable Long id) {
        return ResponseEntity.ok(registrationService.getEventRegistrations(id));
    }

    // User registrations: GET /api/users/me/registrations
    @GetMapping("/users/me/registrations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<RegistrationResponse>> getUserRegistrations(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(registrationService.getUserRegistrations(principal.getId()));
    }

    // Add bookmark: POST /api/events/{id}/bookmark
    @PostMapping("/events/{id}/bookmark")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookmarkResponse> addBookmark(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(bookmarkService.addBookmark(principal.getId(), id));
    }

    // Remove bookmark: DELETE /api/events/{id}/bookmark
    @DeleteMapping("/events/{id}/bookmark")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> removeBookmark(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        bookmarkService.removeBookmark(principal.getId(), id);
        return ResponseEntity.ok(Map.of("message", "Bookmark removed successfully"));
    }

    // User bookmarks: GET /api/users/me/bookmarks
    @GetMapping("/users/me/bookmarks")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BookmarkResponse>> getUserBookmarks(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(bookmarkService.getUserBookmarks(principal.getId()));
    }

    // Total registrations count
    @GetMapping("/registrations/count")
    public ResponseEntity<Map<String, Long>> getRegistrationCount() {
        return ResponseEntity.ok(Map.of("count", registrationService.getTotalRegistrationCount()));
    }
}
