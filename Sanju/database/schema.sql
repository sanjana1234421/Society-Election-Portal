-- ==========================================================
-- Society Voting & Election Management System
-- Database Schema for Offline LAN Environment
-- Database Name: society_voting
-- ==========================================================

CREATE DATABASE IF NOT EXISTS `society_voting` 
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `society_voting`;

-- ----------------------------------------------------------
-- 1. Table: admins
-- ----------------------------------------------------------
DROP TABLE IF EXISTS `election_results`;
DROP TABLE IF EXISTS `votes`;
DROP TABLE IF EXISTS `candidates`;
DROP TABLE IF EXISTS `elections`;
DROP TABLE IF EXISTS `voters`;
DROP TABLE IF EXISTS `admins`;

CREATE TABLE `admins` (
    `admin_id` INT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `full_name` VARCHAR(100) NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- 2. Table: voters
-- ----------------------------------------------------------
CREATE TABLE `voters` (
    `voter_id` INT AUTO_INCREMENT PRIMARY KEY,
    `voter_unique_id` VARCHAR(50) NOT NULL UNIQUE,
    `name` VARCHAR(100) NOT NULL,
    `phone` VARCHAR(20) NOT NULL,
    `flat_number` VARCHAR(20) NOT NULL,
    `password` VARCHAR(255) NOT NULL,
    `status` ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_voter_status` (`status`)
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- 3. Table: elections
-- ----------------------------------------------------------
CREATE TABLE `elections` (
    `election_id` INT AUTO_INCREMENT PRIMARY KEY,
    `election_name` VARCHAR(150) NOT NULL,
    `description` TEXT,
    `start_time` DATETIME NOT NULL,
    `end_time` DATETIME NOT NULL,
    `status` ENUM('UPCOMING', 'ACTIVE', 'CLOSED') DEFAULT 'UPCOMING',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `closed_at` DATETIME DEFAULT NULL,
    `winner_candidate_id` INT DEFAULT NULL,
    CONSTRAINT `chk_election_time` CHECK (`end_time` > `start_time`),
    INDEX `idx_election_status` (`status`)
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- 4. Table: candidates
-- ----------------------------------------------------------
CREATE TABLE `candidates` (
    `candidate_id` INT AUTO_INCREMENT PRIMARY KEY,
    `election_id` INT NOT NULL,
    `candidate_name` VARCHAR(100) NOT NULL,
    `symbol` VARCHAR(50) NOT NULL,
    `description` TEXT,
    `status` ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    CONSTRAINT `fk_candidate_election` 
        FOREIGN KEY (`election_id`) REFERENCES `elections` (`election_id`) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX `idx_candidate_election` (`election_id`)
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- 5. Table: votes (with strict UNIQUE constraint)
-- ----------------------------------------------------------
CREATE TABLE `votes` (
    `vote_id` INT AUTO_INCREMENT PRIMARY KEY,
    `election_id` INT NOT NULL,
    `voter_id` INT NOT NULL,
    `candidate_id` INT NOT NULL,
    `voted_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `uk_election_voter` UNIQUE (`election_id`, `voter_id`),
    CONSTRAINT `fk_vote_election` 
        FOREIGN KEY (`election_id`) REFERENCES `elections` (`election_id`) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_vote_voter` 
        FOREIGN KEY (`voter_id`) REFERENCES `voters` (`voter_id`) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_vote_candidate` 
        FOREIGN KEY (`candidate_id`) REFERENCES `candidates` (`candidate_id`) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX `idx_vote_election` (`election_id`),
    INDEX `idx_vote_candidate` (`candidate_id`)
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- 6. Table: election_results
-- ----------------------------------------------------------
CREATE TABLE `election_results` (
    `result_id` INT AUTO_INCREMENT PRIMARY KEY,
    `election_id` INT NOT NULL,
    `candidate_id` INT NOT NULL,
    `vote_count` INT NOT NULL DEFAULT 0,
    `vote_percentage` DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    `position` INT NOT NULL DEFAULT 1,
    `is_winner` BOOLEAN NOT NULL DEFAULT FALSE,
    `generated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_result_election` 
        FOREIGN KEY (`election_id`) REFERENCES `elections` (`election_id`) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_result_candidate` 
        FOREIGN KEY (`candidate_id`) REFERENCES `candidates` (`candidate_id`) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX `idx_result_election` (`election_id`)
) ENGINE=InnoDB;

-- ==========================================================
-- SEED DATA FOR DEMO & TESTING
-- Default Admin credentials: admin / admin123
-- Default Voter credentials: PIN / password: 1234
-- ==========================================================

-- 1. Insert Default Admin
-- SHA-256 for 'admin123' is 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
INSERT INTO `admins` (`username`, `password`, `full_name`) VALUES 
('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Election Commission Admin');

-- 2. Insert Sample Voters (Password for all is '1234' -> SHA-256: 03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4)
INSERT INTO `voters` (`voter_unique_id`, `name`, `phone`, `flat_number`, `password`, `status`) VALUES 
('VOTER001', 'Rahul Sharma', '9876543210', 'A-101', '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 'ACTIVE'),
('VOTER002', 'Priya Patel', '9876543211', 'B-204', '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 'ACTIVE'),
('VOTER003', 'Vikram Malhotra', '9876543212', 'C-302', '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 'ACTIVE'),
('VOTER004', 'Ananya Deshmukh', '9876543213', 'A-405', '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 'ACTIVE'),
('VOTER005', 'Amitav Sen', '9876543214', 'D-102', '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 'ACTIVE');

-- 3. Insert Sample Active Election
INSERT INTO `elections` (`election_name`, `description`, `start_time`, `end_time`, `status`) VALUES 
('Society Managing Committee Election 2026', 'Annual general election for President of Green Meadows Residential Society.', NOW(), DATE_ADD(NOW(), INTERVAL 2 DAY), 'ACTIVE');

-- 4. Insert Sample Candidates for Election ID 1
INSERT INTO `candidates` (`election_id`, `candidate_name`, `symbol`, `description`, `status`) VALUES 
(1, 'Col. Sandeep Varma', 'Torch', 'Focused on enhanced 24/7 security, CCTV coverage, and disciplined maintenance.', 'ACTIVE'),
(1, 'Dr. Meenakshi Sundaram', 'Tree', 'Championing solar rooftop energy, green landscaping, and waste recycling initiatives.', 'ACTIVE'),
(1, 'Mr. Rajesh Kulkarni', 'Book', 'Advocating for community clubhouse renovations, children library, and transparent accounting.', 'ACTIVE');
