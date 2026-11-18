package com.eventease.service;

import com.eventease.dto.response.RegistrationResponse;
import com.eventease.exception.EventFullException;
import com.eventease.model.Event;
import com.eventease.repository.EventRepository;
import com.eventease.repository.RegistrationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ConcurrentRegistrationTest {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Test
    public void testConcurrentRegistrationsEnforceCapacityStrictly() throws InterruptedException {
        // Create an event with capacity 10
        int capacity = 10;
        Event event = new Event();
        event.setName("High Concurrency Test Event");
        event.setVenue("Concurrency Arena");
        event.setDate(LocalDate.now().plusDays(5));
        event.setCapacity(capacity);
        Event savedEvent = eventRepository.save(event);

        int numberOfThreads = 50;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(numberOfThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger fullCount = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    startSignal.await(); // wait for simultaneous release
                    registrationService.registerParticipant(
                            savedEvent.getId(),
                            "User " + index,
                            "user" + index + "@testconcurrency.com",
                            1000L + index
                    );
                    successCount.incrementAndGet();
                } catch (EventFullException e) {
                    fullCount.incrementAndGet();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    doneSignal.countDown();
                }
            });
        }

        startSignal.countDown(); // unleash all 50 threads concurrently
        boolean finished = doneSignal.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(finished, "All threads should complete within timeout");
        assertEquals(capacity, successCount.get(), "Exactly " + capacity + " registrations should succeed");
        assertEquals(numberOfThreads - capacity, fullCount.get(), "Remaining attempts must fail with EventFullException");

        // Verify state in repository
        assertEquals(capacity, registrationRepository.countByEventId(savedEvent.getId()));
        Event updatedEvent = eventRepository.findById(savedEvent.getId()).orElseThrow();
        assertEquals(capacity, updatedEvent.getRegisteredCount());
        assertEquals("FULL", updatedEvent.getStatus());
    }
}
