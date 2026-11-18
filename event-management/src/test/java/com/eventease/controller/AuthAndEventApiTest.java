package com.eventease.controller;

import com.eventease.dto.request.EventCreateRequest;
import com.eventease.dto.request.LoginRequest;
import com.eventease.dto.request.RegisterRequest;
import com.eventease.dto.response.AuthResponse;
import com.eventease.dto.response.EventResponse;
import com.eventease.dto.response.RegistrationResponse;
import com.eventease.exception.DuplicateRegistrationException;
import com.eventease.security.UserPrincipal;
import com.eventease.service.EventService;
import com.eventease.service.RegistrationService;
import com.eventease.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AuthAndEventApiTest {

    @Autowired
    private AuthController authController;

    @Autowired
    private EventController eventController;

    @Autowired
    private AdminController adminController;

    @Autowired
    private UserService userService;

    @Autowired
    private EventService eventService;

    @Autowired
    private RegistrationService registrationService;

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(UserPrincipal principal) {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    public void testAuthenticationAndRoles() {
        // 1. Seeded Admin login
        LoginRequest adminLogin = new LoginRequest("admin@eventease.com", "admin123");
        ResponseEntity<AuthResponse> adminRes = authController.login(adminLogin);
        assertEquals(HttpStatus.OK, adminRes.getStatusCode());
        assertNotNull(adminRes.getBody());
        assertEquals("ADMIN", adminRes.getBody().getUser().getRole());
        assertNotNull(adminRes.getBody().getToken());

        // 2. Seeded User login
        LoginRequest userLogin = new LoginRequest("john@example.com", "password123");
        ResponseEntity<AuthResponse> userRes = authController.login(userLogin);
        assertEquals(HttpStatus.OK, userRes.getStatusCode());
        assertNotNull(userRes.getBody());
        assertEquals("USER", userRes.getBody().getUser().getRole());

        // 3. User registration
        RegisterRequest registerReq = new RegisterRequest(
                "Diana Prince", "diana" + System.currentTimeMillis() + "@example.com", "password123", "USER");
        ResponseEntity<AuthResponse> registered = authController.register(registerReq);
        assertEquals(HttpStatus.CREATED, registered.getStatusCode());
        assertEquals("Diana Prince", registered.getBody().getUser().getName());
    }

    @Test
    public void testEventLifecycleAndRegistration() {
        UserPrincipal adminPrincipal = new UserPrincipal(userService.getUserEntityByEmail("admin@eventease.com"));
        authenticateAs(adminPrincipal);

        // 1. Create an event as Admin
        EventCreateRequest createRequest = new EventCreateRequest();
        createRequest.setName("Integration Masterclass");
        createRequest.setVenue("Tech Hub");
        createRequest.setDate(LocalDate.now().plusDays(20));
        createRequest.setCapacity(2);

        ResponseEntity<EventResponse> created = eventController.createEvent(createRequest, adminPrincipal);
        assertEquals(HttpStatus.CREATED, created.getStatusCode());
        Long eventId = created.getBody().getId();

        // 2. Register as regular user
        UserPrincipal user1 = new UserPrincipal(userService.getUserEntityByEmail("john@example.com"));
        authenticateAs(user1);

        ResponseEntity<RegistrationResponse> reg1 = eventController.registerForEvent(eventId, null, user1);
        assertEquals(HttpStatus.CREATED, reg1.getStatusCode());
        assertEquals("CONFIRMED", reg1.getBody().getStatus());

        // 3. Duplicate registration should throw DuplicateRegistrationException
        assertThrows(DuplicateRegistrationException.class, () -> {
            eventController.registerForEvent(eventId, null, user1);
        });

        // 4. Cancel registration
        ResponseEntity<?> cancelRes = eventController.cancelRegistration(eventId, user1);
        assertEquals(HttpStatus.OK, cancelRes.getStatusCode());

        // 5. Query all events
        ResponseEntity<List<EventResponse>> events = eventController.getAllEvents(null, null, user1);
        assertEquals(HttpStatus.OK, events.getStatusCode());
        assertTrue(events.getBody().size() >= 1);
    }
}
