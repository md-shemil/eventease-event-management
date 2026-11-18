-- ====================================================================
-- EventEase - Database Initialization Script for Docker Container
-- Automatically executed by MySQL container entrypoint on initial startup
-- ====================================================================

-- 1. Create Database
CREATE DATABASE IF NOT EXISTS event_management
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- 2. Create Application Users & Passwords
CREATE USER IF NOT EXISTS 'eventease'@'localhost' IDENTIFIED BY 'EventEase@2026';
CREATE USER IF NOT EXISTS 'eventease'@'%' IDENTIFIED BY 'EventEase@2026';

-- 3. Grant Permissions
GRANT ALL PRIVILEGES ON event_management.* TO 'eventease'@'localhost';
GRANT ALL PRIVILEGES ON event_management.* TO 'eventease'@'%';
FLUSH PRIVILEGES;

USE event_management;

-- 4. Create Tables
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_users_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    icon VARCHAR(50),
    CONSTRAINT uk_categories_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    venue VARCHAR(255) NOT NULL,
    event_date DATE NOT NULL,
    event_time VARCHAR(50) NOT NULL DEFAULT '10:00 AM',
    capacity INT NOT NULL,
    registered_count INT NOT NULL DEFAULT 0,
    category_id BIGINT,
    organizer_id BIGINT,
    banner_theme VARCHAR(50) DEFAULT 'purple',
    banner_icon VARCHAR(50) DEFAULT '✦',
    status VARCHAR(20) NOT NULL DEFAULT 'UPCOMING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_events_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    CONSTRAINT fk_events_organizer FOREIGN KEY (organizer_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_events_category (category_id),
    INDEX idx_events_date (event_date),
    INDEX idx_events_organizer (organizer_id),
    INDEX idx_events_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS registrations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    user_id BIGINT,
    user_name VARCHAR(100) NOT NULL,
    user_email VARCHAR(150) NOT NULL,
    registration_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
    CONSTRAINT fk_registrations_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    CONSTRAINT uk_registrations_event_user UNIQUE (event_id, user_id),
    INDEX idx_registrations_event (event_id),
    INDEX idx_registrations_user (user_id),
    INDEX idx_registrations_email (user_email),
    INDEX idx_registrations_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS bookmarks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bookmarks_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_bookmarks_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    CONSTRAINT uk_bookmarks_user_event UNIQUE (user_id, event_id),
    INDEX idx_bookmarks_user (user_id),
    INDEX idx_bookmarks_event (event_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_notifications_user (user_id),
    INDEX idx_notifications_read (is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Seed Initial Data
INSERT INTO users (id, name, email, password, role, created_at) VALUES
(1, 'EventEase Admin', 'admin@eventease.com', '$2a$10$xv/iY5bs6lybEYaKRTsBLOSLVwDL1qre3Dc/mR/xUdIsftR5YBjoy', 'ADMIN', NOW()),
(2, 'John Doe', 'john@example.com', '$2a$10$VgEXQMQxoONqK8onK6sj6OnUfvT4dQ0F8GxAe.MVlqGkcdpxboZRS', 'USER', NOW()),
(3, 'Sarah Connor', 'sarah@example.com', '$2a$10$VgEXQMQxoONqK8onK6sj6OnUfvT4dQ0F8GxAe.MVlqGkcdpxboZRS', 'USER', NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name);

INSERT INTO categories (id, name, description, icon) VALUES
(1, 'Technology', 'AI, Cloud Computing, Web3 & Future Tech', '🚀'),
(2, 'Music & Arts', 'Live concerts, indie acoustics & art expos', '🎵'),
(3, 'Workshops', 'Interactive bootcamps & hands-on development', '🎓'),
(4, 'Business & Startups', 'Founder meetups, pitching & venture networks', '💼'),
(5, 'Design & Creative', 'UI/UX, graphic design & digital media', '🎨'),
(6, 'Conferences & Talks', 'Keynotes, panel discussions & summits', '🎤')
ON DUPLICATE KEY UPDATE description=VALUES(description);

INSERT INTO events (id, name, description, venue, event_date, event_time, capacity, registered_count, category_id, organizer_id, banner_theme, banner_icon, status, created_at) VALUES
(1, 'Global Tech Summit 2026', 'Join industry leaders, founders, and engineers exploring next-generation AI and distributed systems.', 'Grand Convention Center, Tech Park', DATE_ADD(CURRENT_DATE, INTERVAL 14 DAY), '09:30 AM', 150, 2, 1, 1, 'purple', '🚀', 'UPCOMING', NOW()),
(2, 'Indie Rhythm Music Festival', 'An evening of vibrant acoustic performances, indie bands, food trucks, and open-air celebration.', 'Skyline Amphitheatre, Downtown', DATE_ADD(CURRENT_DATE, INTERVAL 21 DAY), '05:00 PM', 300, 0, 2, 1, 'pink', '🎵', 'UPCOMING', NOW()),
(3, 'Fullstack Cloud Architecture Masterclass', 'Hands-on deep dive into building production-ready scalable cloud applications with modern microservices.', 'Innovation Lab, SRM Campus', DATE_ADD(CURRENT_DATE, INTERVAL 7 DAY), '10:00 AM', 40, 1, 3, 1, 'blue', '🎓', 'UPCOMING', NOW()),
(4, 'Venture Pitch & Angel Mixer', 'Connect early-stage tech startups with premier angel investors and venture capital syndicates.', 'The Hive Executive Lounge', DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY), '04:30 PM', 80, 0, 4, 1, 'yellow', '💼', 'UPCOMING', NOW()),
(5, 'Creative UI/UX Design Sprint', 'Collaborative interactive design sprint focusing on accessibility, micro-interactions, and modern design systems.', 'Studio 42 Arts Center', DATE_ADD(CURRENT_DATE, INTERVAL 10 DAY), '11:00 AM', 25, 0, 5, 1, 'green', '🎨', 'UPCOMING', NOW()),
(6, 'Future of AI Keynote & Panel', 'A high-impact symposium on ethical AI, generative models, and autonomous agents in software engineering.', 'Civic Auditorium, Hall A', DATE_ADD(CURRENT_DATE, INTERVAL 45 DAY), '02:00 PM', 200, 0, 6, 1, 'orange', '🎤', 'UPCOMING', NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name);

INSERT INTO registrations (id, event_id, user_id, user_name, user_email, registration_date, status) VALUES
(1, 1, 2, 'John Doe', 'john@example.com', NOW(), 'CONFIRMED'),
(2, 1, 3, 'Sarah Connor', 'sarah@example.com', NOW(), 'CONFIRMED'),
(3, 3, 2, 'John Doe', 'john@example.com', NOW(), 'CONFIRMED')
ON DUPLICATE KEY UPDATE status=VALUES(status);

INSERT INTO bookmarks (id, user_id, event_id, created_at) VALUES
(1, 2, 1, NOW()),
(2, 3, 2, NOW())
ON DUPLICATE KEY UPDATE created_at=VALUES(created_at);

INSERT INTO notifications (id, user_id, title, message, is_read, created_at) VALUES
(1, 2, 'Registration Confirmed', 'You have successfully registered for ''Global Tech Summit 2026''.', FALSE, NOW()),
(2, 2, 'Registration Confirmed', 'You have successfully registered for ''Fullstack Cloud Architecture Masterclass''.', FALSE, NOW()),
(3, 3, 'Registration Confirmed', 'You have successfully registered for ''Global Tech Summit 2026''.', FALSE, NOW()),
(4, 2, 'Welcome to EventEase!', 'Explore upcoming tech summits, music festivals, and workshops.', FALSE, NOW()),
(5, 3, 'Welcome to EventEase!', 'Start bookmarking events you want to attend.', FALSE, NOW())
ON DUPLICATE KEY UPDATE title=VALUES(title);
