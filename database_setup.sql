-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
-- Database: library_management
-- ------------------------------------------------------
CREATE DATABASE IF NOT EXISTS library_management;
USE library_management;
/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

-- ------------------------------------------------------
-- Table structure & data for `accounts` & `users`
-- ------------------------------------------------------
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

INSERT INTO `users` VALUES (1,'admin','123456','Admin','admin@library.com','ADMIN'),(2,'staff01','123456','Library Staff 01','staff01@library.com','STAFF');

-- ------------------------------------------------------
-- Table structure & data for `categories`
-- ------------------------------------------------------
DROP TABLE IF EXISTS `categories`;
CREATE TABLE `categories` (
                              `category_id` bigint NOT NULL AUTO_INCREMENT,
                              `name` varchar(255) NOT NULL,
                              PRIMARY KEY (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `categories` VALUES (1,'Theoretical Physics'),(2,'Applied Mathematics'),(3,'Computer Science'),(4,'Astrophysics');

-- ------------------------------------------------------
-- Table structure & data for `authors`
-- ------------------------------------------------------
DROP TABLE IF EXISTS `authors`;
CREATE TABLE `authors` (
                           `author_id` bigint NOT NULL AUTO_INCREMENT,
                           `name` varchar(255) NOT NULL,
                           `author_code` varchar(20) DEFAULT NULL,
                           `birth_year` int DEFAULT NULL,
                           PRIMARY KEY (`author_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `authors` VALUES
                          (1,'Albert Einstein','AE01879',1879),
                          (2,'Stephen Hawking','SH01942',1942),
                          (3,'Isaac Newton','IN01643',1643),
                          (4,'Alan Turing','AT01912',1912);

-- ------------------------------------------------------
-- Table structure & data for `books`
-- ------------------------------------------------------
DROP TABLE IF EXISTS `books`;
CREATE TABLE `books` (
                         `book_id` bigint NOT NULL AUTO_INCREMENT,
                         `title` varchar(255) NOT NULL,
                         `author_id` bigint DEFAULT NULL,
                         `category_id` bigint DEFAULT NULL,
                         `total_quantity` int DEFAULT '0',
                         `available_quantity` int DEFAULT '0',
                         `book_code` varchar(10) DEFAULT NULL,
                         PRIMARY KEY (`book_id`),
                         KEY `author_id` (`author_id`),
                         KEY `category_id` (`category_id`),
                         CONSTRAINT `books_ibfk_1` FOREIGN KEY (`author_id`) REFERENCES `authors` (`author_id`) ON DELETE SET NULL,
                         CONSTRAINT `books_ibfk_2` FOREIGN KEY (`category_id`) REFERENCES `categories` (`category_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `books` VALUES
                        (1,'The Theory of Relativity',1,1,20,20,'P0001'),
                        (2,'A Brief History of Time',2,4,15,15,'P0002'),
                        (3,'Philosophy Naturals and Principal Mathematica',3,2,5,5,'M0001'),
                        (4,'Computing Machine and Intelligence',4,3,30,30,'C0001');

-- ------------------------------------------------------
-- Table structure & data for `borrowers`
-- ------------------------------------------------------
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
                            (1,'NVA0123','Nguyen Van A','nguyenvana@gmail.com','0912345123'),
                            (2,'TTB0987','Tran Thi B','tranthib@gmail.com','0987654987');

-- ------------------------------------------------------
-- Table structure for `transactions` (Empty for fresh start)
-- ------------------------------------------------------
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

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
-- Clean Dump Completed