-- ====================================================================
-- LIBRARY MANAGEMENT SYSTEM - OFFICIAL DATA SETUP
-- ====================================================================

DROP DATABASE IF EXISTS `library_management`;
CREATE DATABASE `library_management`;
USE `library_management`;

-- 1. Table structure for `accounts`
DROP TABLE IF EXISTS `accounts`;
CREATE TABLE `accounts` (
                            `account_id` int NOT NULL AUTO_INCREMENT,
                            `username` varchar(50) NOT NULL,
                            `password` varchar(50) NOT NULL,
                            `role` varchar(20) NOT NULL,
                            PRIMARY KEY (`account_id`),
                            UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `accounts` VALUES (1,'admin','123456','ADMIN'), (2,'staff01','123456','STAFF');

-- 2. Table structure for `users` (System Staff)
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
                         `user_id` int NOT NULL AUTO_INCREMENT,
                         `username` varchar(100) NOT NULL,
                         `password` varchar(100) NOT NULL,
                         `full_name` varchar(255) DEFAULT NULL,
                         `email` varchar(255) DEFAULT NULL,
                         `role` varchar(50) DEFAULT 'STAFF',
                         PRIMARY KEY (`user_id`),
                         UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `users` VALUES
                        (1,'admin','123456','Super Admin','admin@library.com','ADMIN'),
                        (2,'staff01','123456','Library Staff 01','staff01@library.com','STAFF');

-- 3. Table structure for `categories`
DROP TABLE IF EXISTS `categories`;
CREATE TABLE `categories` (
                              `category_id` bigint NOT NULL AUTO_INCREMENT,
                              `name` varchar(255) NOT NULL,
                              PRIMARY KEY (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `categories` VALUES (1,'Theoretical Physics'),(2,'Applied Mathematics'),(3,'Computer Science'),(4,'Astrophysics');

-- 4. Table structure for `authors`
DROP TABLE IF EXISTS `authors`;
CREATE TABLE `authors` (
                           `author_id` bigint NOT NULL AUTO_INCREMENT,
                           `name` varchar(255) NOT NULL,
                           `author_code` varchar(20) DEFAULT NULL,
                           `birth_year` int DEFAULT NULL,
                           PRIMARY KEY (`author_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Standardized Author Codes matching Java generateAuthorCode logic
INSERT INTO `authors` VALUES
                          (1,'Albert Einstein','EIN1879',1879),
                          (2,'Stephen Hawking','HAW1942',1942),
                          (3,'Isaac Newton','NEW1643',1643),
                          (4,'Alan Turing','TUR1912',1912);

-- 5. Table structure for `books`
DROP TABLE IF EXISTS `books`;
CREATE TABLE `books` (
                         `book_id` bigint NOT NULL AUTO_INCREMENT,
                         `title` varchar(255) NOT NULL,
                         `author_id` bigint DEFAULT NULL,
                         `category_id` bigint DEFAULT NULL,
                         `total_quantity` int DEFAULT '0',
                         `available_quantity` int DEFAULT '0',
                         `book_code` varchar(20) DEFAULT NULL,
                         PRIMARY KEY (`book_id`),
                         KEY `author_id` (`author_id`),
                         KEY `category_id` (`category_id`),
                         CONSTRAINT `books_ibfk_1` FOREIGN KEY (`author_id`) REFERENCES `authors` (`author_id`) ON DELETE SET NULL,
                         CONSTRAINT `books_ibfk_2` FOREIGN KEY (`category_id`) REFERENCES `categories` (`category_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Standardized Book Codes using the 3-letter prefix logic (THE, AST, APP, COM)
INSERT INTO `books` VALUES
                        (1,'The Theory of Relativity',1,1,20,20,'THE0001'),
                        (2,'A Brief History of Time',2,4,15,15,'AST0001'),
                        (3,'Philosophiae Naturalis Principia Mathematica',3,2,5,5,'APP0001'),
                        (4,'Computing Machinery and Intelligence',4,3,30,30,'COM0001'),
                        (5,'Black Holes and Baby Universes',2,4,10,10,'AST0002');

-- 6. Table structure for `borrowers`
DROP TABLE IF EXISTS `borrowers`;
CREATE TABLE `borrowers` (
                             `borrower_id` bigint NOT NULL AUTO_INCREMENT,
                             `borrower_code` varchar(20) DEFAULT NULL,
                             `name` varchar(255) NOT NULL,
                             `email` varchar(255) DEFAULT NULL,
                             `phone_number` varchar(20) DEFAULT NULL,
                             PRIMARY KEY (`borrower_id`),
                             UNIQUE KEY `borrower_code` (`borrower_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `borrowers` VALUES
                            (1,'PCA1234','Pham Cong Anh','conganh@gmail.com','09151234'),
                            (2,'NYG5678','Nguyen Y','nguyen.y@gmail.com','08735678');

-- 7. Table structure for `transactions`
DROP TABLE IF EXISTS `transactions`;
CREATE TABLE `transactions` (
                                `tns_id` bigint NOT NULL AUTO_INCREMENT,
                                `book_id` bigint NOT NULL,
                                `borrower_id` bigint NOT NULL,
                                `borrow_date` date NOT NULL,
                                `due_date` date NOT NULL,
                                `return_date` date DEFAULT NULL,
                                `status` varchar(50) DEFAULT 'BORROWED',
                                PRIMARY KEY (`tns_id`),
                                KEY `book_id` (`book_id`),
                                KEY `borrower_id` (`borrower_id`),
                                CONSTRAINT `transactions_ibfk_1` FOREIGN KEY (`book_id`) REFERENCES `books` (`book_id`) ON DELETE CASCADE,
                                CONSTRAINT `transactions_ibfk_2` FOREIGN KEY (`borrower_id`) REFERENCES `borrowers` (`borrower_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;