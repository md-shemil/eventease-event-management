package com.eventease.service;

import com.eventease.dto.response.RegistrationResponse;
import com.eventease.exception.DuplicateRegistrationException;
import com.eventease.exception.ResourceNotFoundException;
import com.eventease.model.Registration;
import com.eventease.repository.RegistrationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final ConcurrentHashMap<Long, Object> eventLocks = new ConcurrentHashMap<>();

    public RegistrationService(RegistrationRepository registrationRepository) {
        this.registrationRepository = registrationRepository;
    }

    public RegistrationResponse registerParticipant(Long eventId, String name, String email, Long userId) {
        if (eventId == null) {
            throw new IllegalArgumentException("Event ID cannot be null");
        }

        String trimmedEmail = email != null ? email.trim().toLowerCase() : "";
        String trimmedName = name != null ? name.trim() : "";

        Object lock = eventLocks.computeIfAbsent(eventId, k -> new Object());

        synchronized (lock) {
            boolean alreadyRegistered = false;
            if (userId != null && registrationRepository.existsByEventIdAndUserId(eventId, userId)) {
                alreadyRegistered = true;
            } else if (!trimmedEmail.isEmpty() && registrationRepository.existsByEventIdAndUserEmail(eventId, trimmedEmail)) {
                alreadyRegistered = true;
            }

            if (alreadyRegistered) {
                throw new DuplicateRegistrationException("Participant is already registered for this event");
            }

            Registration registration = new Registration(eventId, userId, trimmedName, trimmedEmail);
            Registration saved = registrationRepository.save(registration);

            return mapToResponse(saved);
        }
    }

    public void cancelRegistration(Long eventId, Long userId) {
        if (eventId == null || userId == null) return;

        Object lock = eventLocks.computeIfAbsent(eventId, k -> new Object());

        synchronized (lock) {
            Registration registration = registrationRepository.findByEventIdAndUserId(eventId, userId)
                    .orElseThrow(() -> new ResourceNotFoundException("No active registration found for this event"));

            registrationRepository.deleteById(registration.getId());
        }
    }

    public List<RegistrationResponse> getEventRegistrations(Long eventId) {
        return registrationRepository.findByEventId(eventId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<RegistrationResponse> getUserRegistrations(Long userId) {
        return registrationRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<RegistrationResponse> getAllRegistrations() {
        return registrationRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public boolean isUserRegistered(Long eventId, Long userId) {
        if (eventId == null || userId == null) return false;
        return registrationRepository.existsByEventIdAndUserId(eventId, userId);
    }

    public long getTotalRegistrationCount() {
        return registrationRepository.count();
    }

    public long getCountByEventId(Long eventId) {
        return registrationRepository.countByEventId(eventId);
    }

    private RegistrationResponse mapToResponse(Registration reg) {
        RegistrationResponse response = new RegistrationResponse();
        response.setId(reg.getId());
        response.setEventId(reg.getEventId());
        response.setUserId(reg.getUserId());
        response.setUserName(reg.getUserName());
        response.setUserEmail(reg.getUserEmail());
        response.setRegistrationDate(reg.getRegistrationDate());
        response.setStatus(reg.getStatus());
        return response;
    }
}
