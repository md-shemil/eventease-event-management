package com.eventease.repository;

import com.eventease.model.Registration;
import java.util.List;
import java.util.Optional;

public interface RegistrationRepository {
    Optional<Registration> findById(Long id);
    List<Registration> findAll();
    List<Registration> findByEventId(Long eventId);
    List<Registration> findByUserId(Long userId);
    List<Registration> findByUserEmail(String userEmail);
    boolean existsByEventIdAndUserId(Long eventId, Long userId);
    boolean existsByEventIdAndUserEmail(Long eventId, String userEmail);
    Optional<Registration> findByEventIdAndUserId(Long eventId, Long userId);
    Optional<Registration> findByEventIdAndUserEmail(Long eventId, String userEmail);
    long countByEventId(Long eventId);
    long count();
    Registration save(Registration registration);
    void deleteById(Long id);
    void deleteByEventIdAndUserId(Long eventId, Long userId);
}
