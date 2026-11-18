package com.eventease.controller;

import com.eventease.dto.response.BookmarkResponse;
import com.eventease.dto.response.RegistrationResponse;
import com.eventease.dto.response.UserSummaryResponse;
import com.eventease.security.UserPrincipal;
import com.eventease.service.BookmarkService;
import com.eventease.service.RegistrationService;
import com.eventease.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("isAuthenticated()")
public class UserController {

    private final UserService userService;
    private final RegistrationService registrationService;
    private final BookmarkService bookmarkService;

    public UserController(UserService userService,
                          RegistrationService registrationService,
                          BookmarkService bookmarkService) {
        this.userService = userService;
        this.registrationService = registrationService;
        this.bookmarkService = bookmarkService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserSummaryResponse> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(userService.getCurrentUser(principal.getUsername()));
    }

    @GetMapping("/me/registrations")
    public ResponseEntity<List<RegistrationResponse>> getMyRegistrations(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(registrationService.getUserRegistrations(principal.getId()));
    }

    @GetMapping("/me/bookmarks")
    public ResponseEntity<List<BookmarkResponse>> getMyBookmarks(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(bookmarkService.getUserBookmarks(principal.getId()));
    }
}
