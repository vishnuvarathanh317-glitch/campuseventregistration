-- =======================================================
--  Campus Event Registration System — Database Schema
--  Run this script once to set up the database
-- =======================================================

CREATE DATABASE IF NOT EXISTS campus_events
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE campus_events;

-- -------------------------------------------------------
-- USERS TABLE
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100)  NOT NULL,
    email       VARCHAR(150)  NOT NULL UNIQUE,
    password    VARCHAR(255)  NOT NULL,
    salt        VARCHAR(64)   NOT NULL,
    phone       VARCHAR(20),
    department  VARCHAR(100),
    year        INT CHECK (year BETWEEN 1 AND 5),
    role        ENUM('student', 'admin') NOT NULL DEFAULT 'student',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_role  (role)
);

-- -------------------------------------------------------
-- EVENTS TABLE
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS events (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    title        VARCHAR(200) NOT NULL,
    description  TEXT,
    category     VARCHAR(50)  NOT NULL,
    event_date   DATE         NOT NULL,
    start_time   TIME,
    end_time     TIME,
    venue        VARCHAR(200),
    organizer    VARCHAR(150),
    capacity     INT          NOT NULL DEFAULT 100,
    registered   INT          NOT NULL DEFAULT 0,
    image_url    VARCHAR(500),
    rules        TEXT,
    eligibility  TEXT,
    status       ENUM('upcoming','open','full','completed','cancelled') NOT NULL DEFAULT 'upcoming',
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status     (status),
    INDEX idx_category   (category),
    INDEX idx_event_date (event_date)
);

-- -------------------------------------------------------
-- REGISTRATIONS TABLE
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS registrations (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    user_id           INT NOT NULL,
    event_id          INT NOT NULL,
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status            ENUM('confirmed','cancelled') NOT NULL DEFAULT 'confirmed',
    FOREIGN KEY (user_id)  REFERENCES users(id)  ON DELETE CASCADE,
    FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    UNIQUE KEY unique_registration (user_id, event_id),
    INDEX idx_user_id  (user_id),
    INDEX idx_event_id (event_id)
);

-- -------------------------------------------------------
-- TRIGGER: auto-update events.registered count on insert
-- -------------------------------------------------------
DELIMITER $$

CREATE TRIGGER trg_registration_insert
AFTER INSERT ON registrations
FOR EACH ROW
BEGIN
    IF NEW.status = 'confirmed' THEN
        UPDATE events SET registered = registered + 1
        WHERE id = NEW.event_id;
        -- Auto-mark event as full when capacity reached
        UPDATE events
        SET status = 'full'
        WHERE id = NEW.event_id
          AND registered >= capacity
          AND status = 'open';
    END IF;
END$$

CREATE TRIGGER trg_registration_cancel
AFTER UPDATE ON registrations
FOR EACH ROW
BEGIN
    IF OLD.status = 'confirmed' AND NEW.status = 'cancelled' THEN
        UPDATE events SET registered = GREATEST(registered - 1, 0)
        WHERE id = NEW.event_id;
        -- Re-open event if it was full
        UPDATE events
        SET status = 'open'
        WHERE id = NEW.event_id
          AND status = 'full'
          AND registered < capacity;
    END IF;
END$$

DELIMITER ;
