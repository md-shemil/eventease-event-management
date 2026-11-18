-- ====================================================================
-- EventEase - Event Management System
-- Database Seed Data (DML)
-- Passwords are encrypted with standard BCrypt:
-- admin123    -> $2a$10$xv/iY5bs6lybEYaKRTsBLOSLVwDL1qre3Dc/mR/xUdIsftR5YBjoy
-- password123 -> $2a$10$VgEXQMQxoONqK8onK6sj6OnUfvT4dQ0F8GxAe.MVlqGkcdpxboZRS
-- ====================================================================

-- 1. Seed Users
INSERT INTO users (id, name, email, password, role, created_at) VALUES
(1, 'EventEase Admin', 'admin@eventease.com', '$2a$10$xv/iY5bs6lybEYaKRTsBLOSLVwDL1qre3Dc/mR/xUdIsftR5YBjoy', 'ADMIN', NOW()),
(2, 'John Doe', 'john@example.com', '$2a$10$VgEXQMQxoONqK8onK6sj6OnUfvT4dQ0F8GxAe.MVlqGkcdpxboZRS', 'USER', NOW()),
(3, 'Sarah Connor', 'sarah@example.com', '$2a$10$VgEXQMQxoONqK8onK6sj6OnUfvT4dQ0F8GxAe.MVlqGkcdpxboZRS', 'USER', NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 2. Seed Categories
INSERT INTO categories (id, name, description, icon) VALUES
(1, 'Technology', 'AI, Cloud Computing, Web3 & Future Tech', '🚀'),
(2, 'Music & Arts', 'Live concerts, indie acoustics & art expos', '🎵'),
(3, 'Workshops', 'Interactive bootcamps & hands-on development', '🎓'),
(4, 'Business & Startups', 'Founder meetups, pitching & venture networks', '💼'),
(5, 'Design & Creative', 'UI/UX, graphic design & digital media', '🎨'),
(6, 'Conferences & Talks', 'Keynotes, panel discussions & summits', '🎤')
ON DUPLICATE KEY UPDATE description=VALUES(description);

-- 3. Seed Events
INSERT INTO events (id, name, description, venue, event_date, event_time, capacity, registered_count, category_id, organizer_id, banner_theme, banner_icon, status, created_at) VALUES
(1, 'Global Tech Summit 2026', 'Join industry leaders, founders, and engineers exploring next-generation AI and distributed systems.', 'Grand Convention Center, Tech Park', DATE_ADD(CURRENT_DATE, INTERVAL 14 DAY), '09:30 AM', 150, 2, 1, 1, 'purple', '🚀', 'UPCOMING', NOW()),
(2, 'Indie Rhythm Music Festival', 'An evening of vibrant acoustic performances, indie bands, food trucks, and open-air celebration.', 'Skyline Amphitheatre, Downtown', DATE_ADD(CURRENT_DATE, INTERVAL 21 DAY), '05:00 PM', 300, 0, 2, 1, 'pink', '🎵', 'UPCOMING', NOW()),
(3, 'Fullstack Cloud Architecture Masterclass', 'Hands-on deep dive into building production-ready scalable cloud applications with modern microservices.', 'Innovation Lab, SRM Campus', DATE_ADD(CURRENT_DATE, INTERVAL 7 DAY), '10:00 AM', 40, 1, 3, 1, 'blue', '🎓', 'UPCOMING', NOW()),
(4, 'Venture Pitch & Angel Mixer', 'Connect early-stage tech startups with premier angel investors and venture capital syndicates.', 'The Hive Executive Lounge', DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY), '04:30 PM', 80, 0, 4, 1, 'yellow', '💼', 'UPCOMING', NOW()),
(5, 'Creative UI/UX Design Sprint', 'Collaborative interactive design sprint focusing on accessibility, micro-interactions, and modern design systems.', 'Studio 42 Arts Center', DATE_ADD(CURRENT_DATE, INTERVAL 10 DAY), '11:00 AM', 25, 0, 5, 1, 'green', '🎨', 'UPCOMING', NOW()),
(6, 'Future of AI Keynote & Panel', 'A high-impact symposium on ethical AI, generative models, and autonomous agents in software engineering.', 'Civic Auditorium, Hall A', DATE_ADD(CURRENT_DATE, INTERVAL 45 DAY), '02:00 PM', 200, 0, 6, 1, 'orange', '🎤', 'UPCOMING', NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 4. Seed Registrations
INSERT INTO registrations (id, event_id, user_id, user_name, user_email, registration_date, status) VALUES
(1, 1, 2, 'John Doe', 'john@example.com', NOW(), 'CONFIRMED'),
(2, 1, 3, 'Sarah Connor', 'sarah@example.com', NOW(), 'CONFIRMED'),
(3, 3, 2, 'John Doe', 'john@example.com', NOW(), 'CONFIRMED')
ON DUPLICATE KEY UPDATE status=VALUES(status);

-- 5. Seed Bookmarks
INSERT INTO bookmarks (id, user_id, event_id, created_at) VALUES
(1, 2, 1, NOW()),
(2, 3, 2, NOW())
ON DUPLICATE KEY UPDATE created_at=VALUES(created_at);

-- 6. Seed Notifications
INSERT INTO notifications (id, user_id, title, message, is_read, created_at) VALUES
(1, 2, 'Registration Confirmed', 'You have successfully registered for ''Global Tech Summit 2026''.', FALSE, NOW()),
(2, 2, 'Registration Confirmed', 'You have successfully registered for ''Fullstack Cloud Architecture Masterclass''.', FALSE, NOW()),
(3, 3, 'Registration Confirmed', 'You have successfully registered for ''Global Tech Summit 2026''.', FALSE, NOW()),
(4, 2, 'Welcome to EventEase!', 'Explore upcoming tech summits, music festivals, and workshops.', FALSE, NOW()),
(5, 3, 'Welcome to EventEase!', 'Start bookmarking events you want to attend.', FALSE, NOW())
ON DUPLICATE KEY UPDATE title=VALUES(title);
