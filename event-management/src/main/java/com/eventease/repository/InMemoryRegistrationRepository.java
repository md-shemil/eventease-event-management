package com.eventease.repository;

import com.eventease.model.Registration;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InMemoryRegistrationRepository implements RegistrationRepository {

    private final ConcurrentHashMap<Long, Registration> storage = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(0);

    @Override
    public Optional<Registration> findById(Long id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Registration> findAll() {
        return storage.values().stream()
                .sorted(Comparator.comparing(Registration::getRegistrationDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    @Override
    public List<Registration> findByEventId(Long eventId) {
        if (eventId == null) return new ArrayList<>();
        return storage.values().stream()
                .filter(r -> eventId.equals(r.getEventId()) && !"CANCELLED".equalsIgnoreCase(r.getStatus()))
                .sorted(Comparator.comparing(Registration::getRegistrationDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    @Override
    public List<Registration> findByUserId(Long userId) {
        if (userId == null) return new ArrayList<>();
        return storage.values().stream()
                .filter(r -> userId.equals(r.getUserId()) && !"CANCELLED".equalsIgnoreCase(r.getStatus()))
                .sorted(Comparator.comparing(Registration::getRegistrationDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    @Override
    public List<Registration> findByUserEmail(String userEmail) {
        if (userEmail == null) return new ArrayList<>();
        return storage.values().stream()
                .filter(r -> userEmail.equalsIgnoreCase(r.getUserEmail()) && !"CANCELLED".equalsIgnoreCase(r.getStatus()))
                .sorted(Comparator.comparing(Registration::getRegistrationDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByEventIdAndUserId(Long eventId, Long userId) {
        if (eventId == null || userId == null) return false;
        return storage.values().stream()
                .anyMatch(r -> eventId.equals(r.getEventId())
                        && userId.equals(r.getUserId())
                        && !"CANCELLED".equalsIgnoreCase(r.getStatus()));
    }

    @Override
    public boolean existsByEventIdAndUserEmail(Long eventId, String userEmail) {
        if (eventId == null || userEmail == null) return false;
        return storage.values().stream()
                .anyMatch(r -> eventId.equals(r.getEventId())
                        && userEmail.equalsIgnoreCase(r.getUserEmail())
                        && !"CANCELLED".equalsIgnoreCase(r.getStatus()));
    }

    @Override
    public Optional<Registration> findByEventIdAndUserId(Long eventId, Long userId) {
        if (eventId == null || userId == null) return Optional.empty();
        return storage.values().stream()
                .filter(r -> eventId.equals(r.getEventId())
                        && userId.equals(r.getUserId())
                        && !"CANCELLED".equalsIgnoreCase(r.getStatus()))
                .findFirst();
    }

    @Override
    public Optional<Registration> findByEventIdAndUserEmail(Long eventId, String userEmail) {
        if (eventId == null || userEmail == null) return Optional.empty();
        return storage.values().stream()
                .filter(r -> eventId.equals(r.getEventId())
                        && userEmail.equalsIgnoreCase(r.getUserEmail())
                        && !"CANCELLED".equalsIgnoreCase(r.getStatus()))
                .findFirst();
    }

    @Override
    public long countByEventId(Long eventId) {
        if (eventId == null) return 0;
        return storage.values().stream()
                .filter(r -> eventId.equals(r.getEventId()) && !"CANCELLED".equalsIgnoreCase(r.getStatus()))
                .count();
    }

    @Override
    public long count() {
        return storage.values().stream()
                .filter(r -> !"CANCELLED".equalsIgnoreCase(r.getStatus()))
                .count();
    }

    @Override
    public Registration save(Registration registration) {
        if (registration.getId() == null) {
            registration.setId(idSequence.incrementAndGet());
        }
        storage.put(registration.getId(), registration);
        return registration;
    }

    @Override
    public void deleteById(Long id) {
        if (id != null) {
            storage.remove(id);
        }
    }

    @Override
    public void deleteByEventIdAndUserId(Long eventId, Long userId) {
        if (eventId == null || userId == null) return;
        storage.values().removeIf(r -> eventId.equals(r.getEventId()) && userId.equals(r.getUserId()));
    }
}
