package com.eventease.service;

import com.eventease.dto.response.RegistrationResponse;
import com.eventease.exception.DuplicateRegistrationException;
import com.eventease.exception.EventFullException;
import com.eventease.exception.EventNotFoundException;
import com.eventease.exception.ResourceNotFoundException;
import com.eventease.model.Event;
import com.eventease.model.Registration;
import com.eventease.repository.EventRepository;
import com.eventease.repository.RegistrationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class RegistrationService {

    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final NotificationService notificationService;

    // Per-event monitor locks for thread-safe concurrent registration & capacity enforcement
    private final ConcurrentHashMap<Long, Object> eventLocks = new ConcurrentHashMap<>();

    public RegistrationService(EventRepository eventRepository,
                               RegistrationRepository registrationRepository,
                               NotificationService notificationService) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.notificationService = notificationService;
    }

    public RegistrationResponse registerParticipant(Long eventId, String name, String email, Long userId) {
        if (eventId == null) {
            throw new IllegalArgumentException("Event ID cannot be null");
        }

        String trimmedEmail = email != null ? email.trim().toLowerCase() : "";
        String trimmedName = name != null ? name.trim() : "";

        Object lock = eventLocks.computeIfAbsent(eventId, k -> new Object());

        synchronized (lock) {
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + eventId));

            // Check if already registered
            boolean alreadyRegistered = false;
            if (userId != null && registrationRepository.existsByEventIdAndUserId(eventId, userId)) {
                alreadyRegistered = true;
            } else if (!trimmedEmail.isEmpty() && registrationRepository.existsByEventIdAndUserEmail(eventId, trimmedEmail)) {
                alreadyRegistered = true;
            }

            if (alreadyRegistered) {
                throw new DuplicateRegistrationException("Participant is already registered for this event");
            }

            // Check capacity validation
            long currentCount = registrationRepository.countByEventId(eventId);
            if (currentCount >= event.getCapacity()) {
                event.setStatus("FULL");
                eventRepository.save(event);
                throw new EventFullException("Event has reached its maximum capacity of " + event.getCapacity());
            }

            // Create and save registration
            Registration registration = new Registration(eventId, userId, trimmedName, trimmedEmail);
            Registration saved = registrationRepository.save(registration);

            // Atomically update event count and status
            int newCount = (int) (currentCount + 1);
            event.setRegisteredCount(newCount);
            if (newCount >= event.getCapacity()) {
                event.setStatus("FULL");
            }
            eventRepository.save(event);

            // Send notification
            if (userId != null) {
                notificationService.createNotification(
                        userId,
                        "Registration Confirmed",
                        "You have successfully registered for '" + event.getName() + "'."
                );
            }

            return mapToResponse(saved, event);
        }
    }

    public void cancelRegistration(Long eventId, Long userId) {
        if (eventId == null || userId == null) return;

        Object lock = eventLocks.computeIfAbsent(eventId, k -> new Object());

        synchronized (lock) {
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + eventId));

            Registration registration = registrationRepository.findByEventIdAndUserId(eventId, userId)
                    .orElseThrow(() -> new ResourceNotFoundException("No active registration found for this event"));

            registrationRepository.deleteById(registration.getId());

            long currentCount = registrationRepository.countByEventId(eventId);
            event.setRegisteredCount((int) currentCount);
            if (currentCount < event.getCapacity() && "FULL".equalsIgnoreCase(event.getStatus())) {
                event.setStatus("UPCOMING");
            }
            eventRepository.save(event);

            notificationService.createNotification(
                    userId,
                    "Registration Cancelled",
                    "Your registration for '" + event.getName() + "' has been cancelled."
            );
        }
    }

    public List<RegistrationResponse> getEventRegistrations(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + eventId));

        return registrationRepository.findByEventId(eventId).stream()
                .map(r -> mapToResponse(r, event))
                .collect(Collectors.toList());
    }

    public List<RegistrationResponse> getUserRegistrations(Long userId) {
        return registrationRepository.findByUserId(userId).stream()
                .map(r -> {
                    Event event = eventRepository.findById(r.getEventId()).orElse(null);
                    return mapToResponse(r, event);
                })
                .collect(Collectors.toList());
    }

    public List<RegistrationResponse> getAllRegistrations() {
        return registrationRepository.findAll().stream()
                .map(r -> {
                    Event event = eventRepository.findById(r.getEventId()).orElse(null);
                    return mapToResponse(r, event);
                })
                .collect(Collectors.toList());
    }

    public boolean isUserRegistered(Long eventId, Long userId) {
        if (eventId == null || userId == null) return false;
        return registrationRepository.existsByEventIdAndUserId(eventId, userId);
    }

    public long getTotalRegistrationCount() {
        return registrationRepository.count();
    }

    private RegistrationResponse mapToResponse(Registration reg, Event event) {
        RegistrationResponse response = new RegistrationResponse();
        response.setId(reg.getId());
        response.setEventId(reg.getEventId());
        response.setUserId(reg.getUserId());
        response.setUserName(reg.getUserName());
        response.setUserEmail(reg.getUserEmail());
        response.setRegistrationDate(reg.getRegistrationDate());
        response.setStatus(reg.getStatus());

        if (event != null) {
            response.setEventName(event.getName());
            response.setEventVenue(event.getVenue());
            response.setEventDate(event.getDate());
            response.setEventTime(event.getTime());
        }

        return response;
    }
}
