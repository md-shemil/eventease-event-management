package com.eventease.repository;

import com.eventease.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class JdbcRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    public void testUserJdbcRepository() {
        String email = "testuser_" + System.currentTimeMillis() + "@eventease.com";
        User user = new User("Test Runner", email, "secretHash", Role.USER);
        User saved = userRepository.save(user);

        assertNotNull(saved.getId(), "Generated ID should not be null");
        assertTrue(userRepository.existsByEmail(email));

        Optional<User> byEmail = userRepository.findByEmail(email);
        assertTrue(byEmail.isPresent());
        assertEquals("Test Runner", byEmail.get().getName());

        saved.setName("Updated Test Runner");
        userRepository.save(saved);
        Optional<User> updated = userRepository.findById(saved.getId());
        assertTrue(updated.isPresent());
        assertEquals("Updated Test Runner", updated.get().getName());

        userRepository.deleteById(saved.getId());
        assertFalse(userRepository.findById(saved.getId()).isPresent());
    }

    @Test
    public void testCategoryAndEventJdbcRepository() {
        Category cat = new Category("Design Sprint " + System.currentTimeMillis(), "Design desc", "🎨");
        Category savedCat = categoryRepository.save(cat);
        assertNotNull(savedCat.getId());

        Event event = new Event();
        event.setName("JDBC Deep Dive Expo " + System.currentTimeMillis());
        event.setDescription("Deep dive into JDBC Template and connection pooling");
        event.setVenue("Tech Lab 1");
        event.setDate(LocalDate.now().plusDays(10));
        event.setCapacity(50);
        event.setCategoryId(savedCat.getId());
        event.setBannerTheme("blue");
        event.setBannerIcon("💾");

        Event savedEvent = eventRepository.save(event);
        assertNotNull(savedEvent.getId());
        assertTrue(eventRepository.existsById(savedEvent.getId()));

        // Test search
        List<Event> searchResults = eventRepository.search("Deep Dive");
        assertFalse(searchResults.isEmpty());

        // Test category filter
        List<Event> catEvents = eventRepository.findByCategoryId(savedCat.getId());
        assertEquals(1, catEvents.size());

        // Cleanup
        eventRepository.deleteById(savedEvent.getId());
        assertFalse(eventRepository.existsById(savedEvent.getId()));
        categoryRepository.deleteById(savedCat.getId());
    }

    @Test
    public void testRegistrationBookmarkAndNotification() {
        // Setup user and event
        User user = userRepository.save(new User("Participant", "part_" + System.currentTimeMillis() + "@test.com", "pass", Role.USER));
        Event event = eventRepository.save(new Event("Seminar", "Hall B", LocalDate.now().plusDays(5), 20));

        // Registration
        Registration reg = new Registration(event.getId(), user.getId(), user.getName(), user.getEmail());
        Registration savedReg = registrationRepository.save(reg);
        assertNotNull(savedReg.getId());
        assertTrue(registrationRepository.existsByEventIdAndUserId(event.getId(), user.getId()));
        assertEquals(1, registrationRepository.countByEventId(event.getId()));

        // Bookmark
        Bookmark bookmark = bookmarkRepository.save(new Bookmark(user.getId(), event.getId()));
        assertNotNull(bookmark.getId());
        assertTrue(bookmarkRepository.existsByUserIdAndEventId(user.getId(), event.getId()));

        // Notification
        Notification notif = notificationRepository.save(new Notification(user.getId(), "Welcome", "Test message"));
        assertNotNull(notif.getId());
        assertEquals(1, notificationRepository.countUnreadByUserId(user.getId()));

        notif.setRead(true);
        notificationRepository.save(notif);
        assertEquals(0, notificationRepository.countUnreadByUserId(user.getId()));

        // Clean up
        bookmarkRepository.deleteByUserIdAndEventId(user.getId(), event.getId());
        assertFalse(bookmarkRepository.existsByUserIdAndEventId(user.getId(), event.getId()));

        registrationRepository.deleteById(savedReg.getId());
        assertEquals(0, registrationRepository.countByEventId(event.getId()));

        notificationRepository.deleteById(notif.getId());
        eventRepository.deleteById(event.getId());
        userRepository.deleteById(user.getId());
    }
}
