package com.eventease.service;

import com.eventease.dto.response.BookmarkResponse;
import com.eventease.model.Bookmark;
import com.eventease.repository.BookmarkRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;

    public BookmarkService(BookmarkRepository bookmarkRepository) {
        this.bookmarkRepository = bookmarkRepository;
    }

    public BookmarkResponse addBookmark(Long userId, Long eventId) {
        if (!bookmarkRepository.existsByUserIdAndEventId(userId, eventId)) {
            Bookmark bookmark = new Bookmark(userId, eventId);
            bookmarkRepository.save(bookmark);
        }

        Bookmark bookmark = bookmarkRepository.findByUserIdAndEventId(userId, eventId).orElseThrow();
        return mapToResponse(bookmark);
    }

    public void removeBookmark(Long userId, Long eventId) {
        bookmarkRepository.deleteByUserIdAndEventId(userId, eventId);
    }

    public List<BookmarkResponse> getUserBookmarks(Long userId) {
        return bookmarkRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public boolean isBookmarked(Long userId, Long eventId) {
        if (userId == null || eventId == null) return false;
        return bookmarkRepository.existsByUserIdAndEventId(userId, eventId);
    }

    private BookmarkResponse mapToResponse(Bookmark bookmark) {
        BookmarkResponse response = new BookmarkResponse();
        response.setId(bookmark.getId());
        response.setEventId(bookmark.getEventId());
        response.setCreatedAt(bookmark.getCreatedAt());
        return response;
    }
}
