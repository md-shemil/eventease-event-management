package com.eventease.service;

import com.eventease.dto.response.BookmarkResponse;
import com.eventease.exception.EventNotFoundException;
import com.eventease.model.Bookmark;
import com.eventease.model.Event;
import com.eventease.repository.BookmarkRepository;
import com.eventease.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final EventRepository eventRepository;

    public BookmarkService(BookmarkRepository bookmarkRepository,
                           EventRepository eventRepository) {
        this.bookmarkRepository = bookmarkRepository;
        this.eventRepository = eventRepository;
    }

    public BookmarkResponse addBookmark(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + eventId));

        if (!bookmarkRepository.existsByUserIdAndEventId(userId, eventId)) {
            Bookmark bookmark = new Bookmark(userId, eventId);
            bookmarkRepository.save(bookmark);
        }

        Bookmark bookmark = bookmarkRepository.findByUserIdAndEventId(userId, eventId).orElseThrow();
        return mapToResponse(bookmark, event);
    }

    public void removeBookmark(Long userId, Long eventId) {
        bookmarkRepository.deleteByUserIdAndEventId(userId, eventId);
    }

    public List<BookmarkResponse> getUserBookmarks(Long userId) {
        return bookmarkRepository.findByUserId(userId).stream()
                .map(b -> {
                    Event event = eventRepository.findById(b.getEventId()).orElse(null);
                    return mapToResponse(b, event);
                })
                .collect(Collectors.toList());
    }

    public boolean isBookmarked(Long userId, Long eventId) {
        if (userId == null || eventId == null) return false;
        return bookmarkRepository.existsByUserIdAndEventId(userId, eventId);
    }

    private BookmarkResponse mapToResponse(Bookmark bookmark, Event event) {
        BookmarkResponse response = new BookmarkResponse();
        response.setId(bookmark.getId());
        response.setEventId(bookmark.getEventId());
        response.setCreatedAt(bookmark.getCreatedAt());

        if (event != null) {
            response.setEventName(event.getName());
            response.setEventVenue(event.getVenue());
            response.setEventDate(event.getDate());
            response.setBannerTheme(event.getBannerTheme());
            response.setBannerIcon(event.getBannerIcon());
        }

        return response;
    }
}
