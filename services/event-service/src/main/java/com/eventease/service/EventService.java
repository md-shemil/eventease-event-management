package com.eventease.service;

import com.eventease.dto.request.EventCreateRequest;
import com.eventease.dto.request.EventUpdateRequest;
import com.eventease.dto.response.EventResponse;
import com.eventease.exception.BadRequestException;
import com.eventease.exception.EventNotFoundException;
import com.eventease.model.Category;
import com.eventease.model.Event;
import com.eventease.repository.CategoryRepository;
import com.eventease.repository.EventRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;

    public EventService(EventRepository eventRepository,
                        CategoryRepository categoryRepository) {
        this.eventRepository = eventRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<EventResponse> getAllEvents(Long categoryId, String search, Long currentUserId) {
        List<Event> events;

        if (categoryId != null && categoryId > 0) {
            events = eventRepository.findByCategoryId(categoryId);
            if (search != null && !search.isBlank()) {
                String q = search.trim().toLowerCase();
                events = events.stream()
                        .filter(e -> (e.getName() != null && e.getName().toLowerCase().contains(q))
                                || (e.getVenue() != null && e.getVenue().toLowerCase().contains(q))
                                || (e.getDescription() != null && e.getDescription().toLowerCase().contains(q)))
                        .collect(Collectors.toList());
            }
        } else if (search != null && !search.isBlank()) {
            events = eventRepository.search(search);
        } else {
            events = eventRepository.findAll();
        }

        return events.stream()
                .map(e -> mapToResponse(e, currentUserId))
                .collect(Collectors.toList());
    }

    public EventResponse getEventById(Long id, Long currentUserId) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + id));
        return mapToResponse(event, currentUserId);
    }

    public Event getEventEntity(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + id));
    }

    public EventResponse createEvent(EventCreateRequest request, Long organizerId) {
        if (request.getCapacity() <= 0) {
            throw new BadRequestException("Capacity must be greater than zero");
        }

        Event event = new Event();
        event.setName(request.getName().trim());
        event.setDescription(request.getDescription());
        event.setVenue(request.getVenue().trim());
        event.setDate(request.getDate());
        event.setTime(request.getTime() != null && !request.getTime().isBlank() ? request.getTime() : "10:00 AM");
        event.setCapacity(request.getCapacity());
        event.setCategoryId(request.getCategoryId());
        event.setOrganizerId(organizerId);

        if (request.getBannerTheme() != null && !request.getBannerTheme().isBlank()) {
            event.setBannerTheme(request.getBannerTheme());
        }
        if (request.getBannerIcon() != null && !request.getBannerIcon().isBlank()) {
            event.setBannerIcon(request.getBannerIcon());
        }

        Event saved = eventRepository.save(event);
        return mapToResponse(saved, organizerId);
    }

    public EventResponse updateEvent(Long id, EventUpdateRequest request, Long userId, boolean isAdmin) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + id));

        if (!isAdmin && (event.getOrganizerId() == null || !event.getOrganizerId().equals(userId))) {
            throw new AccessDeniedException("You do not have permission to edit this event");
        }

        if (request.getName() != null && !request.getName().isBlank()) {
            event.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getVenue() != null && !request.getVenue().isBlank()) {
            event.setVenue(request.getVenue().trim());
        }
        if (request.getDate() != null) {
            event.setDate(request.getDate());
        }
        if (request.getTime() != null && !request.getTime().isBlank()) {
            event.setTime(request.getTime());
        }
        if (request.getCapacity() != null) {
            if (request.getCapacity() < event.getRegisteredCount()) {
                throw new BadRequestException("Capacity cannot be reduced below current registrations ("
                        + event.getRegisteredCount() + ")");
            }
            event.setCapacity(request.getCapacity());
            if (event.getRegisteredCount() < event.getCapacity() && "FULL".equalsIgnoreCase(event.getStatus())) {
                event.setStatus("UPCOMING");
            }
        }
        if (request.getCategoryId() != null) {
            event.setCategoryId(request.getCategoryId());
        }
        if (request.getBannerTheme() != null) {
            event.setBannerTheme(request.getBannerTheme());
        }
        if (request.getBannerIcon() != null) {
            event.setBannerIcon(request.getBannerIcon());
        }
        if (request.getStatus() != null) {
            event.setStatus(request.getStatus());
        }

        Event updated = eventRepository.save(event);
        return mapToResponse(updated, userId);
    }

    public void deleteEvent(Long id, Long userId, boolean isAdmin) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + id));

        if (!isAdmin && (event.getOrganizerId() == null || !event.getOrganizerId().equals(userId))) {
            throw new AccessDeniedException("You do not have permission to delete this event");
        }

        eventRepository.deleteById(id);
    }

    public synchronized Event incrementRegisteredCount(Long id) {
        Event event = getEventEntity(id);
        if (event.getRegisteredCount() >= event.getCapacity()) {
            throw new BadRequestException("Event is full");
        }
        int newCount = event.getRegisteredCount() + 1;
        event.setRegisteredCount(newCount);
        if (newCount >= event.getCapacity()) {
            event.setStatus("FULL");
        }
        return eventRepository.save(event);
    }

    public synchronized Event decrementRegisteredCount(Long id) {
        Event event = getEventEntity(id);
        int newCount = Math.max(0, event.getRegisteredCount() - 1);
        event.setRegisteredCount(newCount);
        if (newCount < event.getCapacity() && "FULL".equalsIgnoreCase(event.getStatus())) {
            event.setStatus("UPCOMING");
        }
        return eventRepository.save(event);
    }

    public List<EventResponse> getAdminEvents() {
        return eventRepository.findAll().stream()
                .map(e -> mapToResponse(e, null))
                .collect(Collectors.toList());
    }

    public long getTotalEventCount() {
        return eventRepository.count();
    }

    public long getUpcomingEventCount() {
        LocalDate today = LocalDate.now();
        return eventRepository.findAll().stream()
                .filter(e -> e.getDate() != null && !e.getDate().isBefore(today))
                .count();
    }

    private EventResponse mapToResponse(Event event, Long currentUserId) {
        EventResponse resp = new EventResponse();
        resp.setId(event.getId());
        resp.setName(event.getName());
        resp.setDescription(event.getDescription());
        resp.setVenue(event.getVenue());
        resp.setDate(event.getDate());
        resp.setTime(event.getTime());
        resp.setCapacity(event.getCapacity());
        resp.setRegisteredCount(event.getRegisteredCount());
        resp.setAvailableCapacity(Math.max(0, event.getCapacity() - event.getRegisteredCount()));

        resp.setCategoryId(event.getCategoryId());
        if (event.getCategoryId() != null) {
            Category cat = categoryRepository.findById(event.getCategoryId()).orElse(null);
            if (cat != null) {
                resp.setCategoryName(cat.getName());
                resp.setCategoryIcon(cat.getIcon());
            }
        }

        resp.setOrganizerId(event.getOrganizerId());
        resp.setBannerTheme(event.getBannerTheme());
        resp.setBannerIcon(event.getBannerIcon());
        resp.setStatus(event.getStatus());
        resp.setBookmarked(false);
        resp.setRegistered(false);

        return resp;
    }
}
