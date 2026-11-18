package com.eventease.config;

import com.eventease.model.Category;
import com.eventease.model.Event;
import com.eventease.model.Role;
import com.eventease.model.User;
import com.eventease.repository.CategoryRepository;
import com.eventease.repository.EventRepository;
import com.eventease.repository.UserRepository;
import com.eventease.service.RegistrationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final RegistrationService registrationService;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           CategoryRepository categoryRepository,
                           EventRepository eventRepository,
                           RegistrationService registrationService,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.eventRepository = eventRepository;
        this.registrationService = registrationService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        // Seed Users
        User admin = new User("EventEase Admin", "admin@eventease.com", passwordEncoder.encode("admin123"), Role.ADMIN);
        userRepository.save(admin);

        User john = new User("John Doe", "john@example.com", passwordEncoder.encode("password123"), Role.USER);
        userRepository.save(john);

        User sarah = new User("Sarah Connor", "sarah@example.com", passwordEncoder.encode("password123"), Role.USER);
        userRepository.save(sarah);

        // Seed Categories
        Category catTech = categoryRepository.save(new Category("Technology", "AI, Cloud Computing, Web3 & Future Tech", "🚀"));
        Category catMusic = categoryRepository.save(new Category("Music & Arts", "Live concerts, indie acoustics & art expos", "🎵"));
        Category catWorkshop = categoryRepository.save(new Category("Workshops", "Interactive bootcamps & hands-on development", "🎓"));
        Category catBusiness = categoryRepository.save(new Category("Business & Startups", "Founder meetups, pitching & venture networks", "💼"));
        Category catDesign = categoryRepository.save(new Category("Design & Creative", "UI/UX, graphic design & digital media", "🎨"));
        Category catKeynote = categoryRepository.save(new Category("Conferences & Talks", "Keynotes, panel discussions & summits", "🎤"));

        // Seed Events
        Event event1 = new Event();
        event1.setName("Global Tech Summit 2026");
        event1.setDescription("Join industry leaders, founders, and engineers exploring next-generation AI and distributed systems.");
        event1.setVenue("Grand Convention Center, Tech Park");
        event1.setDate(LocalDate.now().plusDays(14));
        event1.setTime("09:30 AM");
        event1.setCapacity(150);
        event1.setCategoryId(catTech.getId());
        event1.setOrganizerId(admin.getId());
        event1.setBannerTheme("purple");
        event1.setBannerIcon("🚀");
        eventRepository.save(event1);

        Event event2 = new Event();
        event2.setName("Indie Rhythm Music Festival");
        event2.setDescription("An evening of vibrant acoustic performances, indie bands, food trucks, and open-air celebration.");
        event2.setVenue("Skyline Amphitheatre, Downtown");
        event2.setDate(LocalDate.now().plusDays(21));
        event2.setTime("05:00 PM");
        event2.setCapacity(300);
        event2.setCategoryId(catMusic.getId());
        event2.setOrganizerId(admin.getId());
        event2.setBannerTheme("pink");
        event2.setBannerIcon("🎵");
        eventRepository.save(event2);

        Event event3 = new Event();
        event3.setName("Fullstack Cloud Architecture Masterclass");
        event3.setDescription("Hands-on deep dive into building production-ready scalable cloud applications with modern microservices.");
        event3.setVenue("Innovation Lab, SRM Campus");
        event3.setDate(LocalDate.now().plusDays(7));
        event3.setTime("10:00 AM");
        event3.setCapacity(40);
        event3.setCategoryId(catWorkshop.getId());
        event3.setOrganizerId(admin.getId());
        event3.setBannerTheme("blue");
        event3.setBannerIcon("🎓");
        eventRepository.save(event3);

        Event event4 = new Event();
        event4.setName("Venture Pitch & Angel Mixer");
        event4.setDescription("Connect early-stage tech startups with premier angel investors and venture capital syndicates.");
        event4.setVenue("The Hive Executive Lounge");
        event4.setDate(LocalDate.now().plusDays(30));
        event4.setTime("04:30 PM");
        event4.setCapacity(80);
        event4.setCategoryId(catBusiness.getId());
        event4.setOrganizerId(admin.getId());
        event4.setBannerTheme("yellow");
        event4.setBannerIcon("💼");
        eventRepository.save(event4);

        Event event5 = new Event();
        event5.setName("Creative UI/UX Design Sprint");
        event5.setDescription("Collaborative interactive design sprint focusing on accessibility, micro-interactions, and modern design systems.");
        event5.setVenue("Studio 42 Arts Center");
        event5.setDate(LocalDate.now().plusDays(10));
        event5.setTime("11:00 AM");
        event5.setCapacity(25);
        event5.setCategoryId(catDesign.getId());
        event5.setOrganizerId(admin.getId());
        event5.setBannerTheme("green");
        event5.setBannerIcon("🎨");
        eventRepository.save(event5);

        Event event6 = new Event();
        event6.setName("Future of AI Keynote & Panel");
        event6.setDescription("A high-impact symposium on ethical AI, generative models, and autonomous agents in software engineering.");
        event6.setVenue("Civic Auditorium, Hall A");
        event6.setDate(LocalDate.now().plusDays(45));
        event6.setTime("02:00 PM");
        event6.setCapacity(200);
        event6.setCategoryId(catKeynote.getId());
        event6.setOrganizerId(admin.getId());
        event6.setBannerTheme("orange");
        event6.setBannerIcon("🎤");
        eventRepository.save(event6);

        // Seed initial registrations
        try {
            registrationService.registerParticipant(event1.getId(), john.getName(), john.getEmail(), john.getId());
            registrationService.registerParticipant(event1.getId(), sarah.getName(), sarah.getEmail(), sarah.getId());
            registrationService.registerParticipant(event3.getId(), john.getName(), john.getEmail(), john.getId());
        } catch (Exception ignored) {
        }
    }
}
