DROP TABLE IF EXISTS `Authors`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `Authors` (
  `author_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `fullname` varchar(255) NOT NULL,
  `author_code` varchar(20) DEFAULT NULL,
  `birth_year` int(11) DEFAULT NULL,
  PRIMARY KEY (`author_id`),
  UNIQUE KEY `unique_author_code` (`author_code`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Authors`
--

LOCK TABLES `Authors` WRITE;
/*!40000 ALTER TABLE `Authors` DISABLE KEYS */;
INSERT INTO `Authors` VALUES
(1,'Albert Einstein','EIN1879',1879),
(2,'Stephen Hawking','HAW1942',1942),
(3,'Isaac Newton','NEW1643',1643),
(4,'Alan Turing','TUR1912',1912);
/*!40000 ALTER TABLE `Authors` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Books`
--

DROP TABLE IF EXISTS `Books`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `Books` (
  `book_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `author_id` bigint(20) DEFAULT NULL,
  `category_id` bigint(20) DEFAULT NULL,
  `total_quantity` int(11) DEFAULT 0,
  `available_quantity` int(11) DEFAULT 0,
  `book_code` varchar(20) NOT NULL,
  PRIMARY KEY (`book_id`),
  UNIQUE KEY `book_code` (`book_code`),
  KEY `author_id` (`author_id`),
  KEY `category_id` (`category_id`),
  KEY `idx_books_title` (`title`),
  CONSTRAINT `books_ibfk_1` FOREIGN KEY (`author_id`) REFERENCES `Authors` (`author_id`) ON DELETE SET NULL,
  CONSTRAINT `books_ibfk_2` FOREIGN KEY (`category_id`) REFERENCES `Categories` (`category_id`) ON DELETE SET NULL,
  CONSTRAINT `chk_quantity` CHECK (`available_quantity` <= `total_quantity`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Books`
--

LOCK TABLES `Books` WRITE;
/*!40000 ALTER TABLE `Books` DISABLE KEYS */;
INSERT INTO `Books` VALUES
(1,'The Theory of Relativity',1,1,20,20,'THE0001'),
(2,'A Brief History of Time',2,4,15,15,'AST0001'),
(3,'Philosophiae Naturalis Principia Mathematica',3,2,5,5,'APP0001'),
(4,'Computing Machinery and Intelligence',4,3,30,30,'COM0001'),
(5,'Black Holes and Baby Universes',2,4,10,10,'AST0002');
/*!40000 ALTER TABLE `Books` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Borrowers`
--

DROP TABLE IF EXISTS `Borrowers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `Borrowers` (
  `borrower_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `borrower_code` varchar(20) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `email` varchar(255) DEFAULT NULL,
  `phone_number` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`borrower_id`),
  UNIQUE KEY `borrower_code` (`borrower_code`),
  UNIQUE KEY `unique_email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Borrowers`
--

LOCK TABLES `Borrowers` WRITE;
/*!40000 ALTER TABLE `Borrowers` DISABLE KEYS */;
INSERT INTO `Borrowers` VALUES
(1,'PCA1234','Pham Cong Anh','conganh@gmail.com','09151234'),
(2,'NYG5678','Nguyen Y','nguyen.y@gmail.com','08735678');
/*!40000 ALTER TABLE `Borrowers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Categories`
--

DROP TABLE IF EXISTS `Categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `Categories` (
  `category_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Categories`
--

LOCK TABLES `Categories` WRITE;
/*!40000 ALTER TABLE `Categories` DISABLE KEYS */;
INSERT INTO `Categories` VALUES
(1,'Theoretical Physics'),
(2,'Applied Mathematics'),
(3,'Computer Science'),
(4,'Astrophysics');
/*!40000 ALTER TABLE `Categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `DamagedBooks`
--

DROP TABLE IF EXISTS `DamagedBooks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `DamagedBooks` (
  `issue_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `book_id` bigint(20) NOT NULL,
  `damage_type` varchar(50) DEFAULT NULL,
  `notes` text DEFAULT NULL,
  `status` enum('UNFIXED','REPAIRED') DEFAULT NULL,
  `reported_date` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`issue_id`),
  KEY `book_id` (`book_id`),
  CONSTRAINT `damagedbooks_ibfk_1` FOREIGN KEY (`book_id`) REFERENCES `Books` (`book_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `DamagedBooks`
--

LOCK TABLES `DamagedBooks` WRITE;
/*!40000 ALTER TABLE `DamagedBooks` DISABLE KEYS */;
/*!40000 ALTER TABLE `DamagedBooks` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Transactions`
--

DROP TABLE IF EXISTS `Transactions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `Transactions` (
  `tns_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `book_id` bigint(20) NOT NULL,
  `borrower_id` bigint(20) NOT NULL,
  `borrow_date` date NOT NULL,
  `due_date` date NOT NULL,
  `return_date` date DEFAULT NULL,
  `status` enum('BORROWED','RETURNED','OVERDUE') DEFAULT 'BORROWED',
  PRIMARY KEY (`tns_id`),
  KEY `book_id` (`book_id`),
  KEY `borrower_id` (`borrower_id`),
  KEY `idx_transactions_due_date` (`due_date`),
  KEY `idx_borrower_status` (`borrower_id`,`status`),
  CONSTRAINT `transactions_ibfk_1` FOREIGN KEY (`book_id`) REFERENCES `Books` (`book_id`),
  CONSTRAINT `transactions_ibfk_2` FOREIGN KEY (`borrower_id`) REFERENCES `Borrowers` (`borrower_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Transactions`
--

LOCK TABLES `Transactions` WRITE;
/*!40000 ALTER TABLE `Transactions` DISABLE KEYS */;
/*!40000 ALTER TABLE `Transactions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Users`
--

DROP TABLE IF EXISTS `Users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `Users` (
  `user_id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `fullname` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `role` enum('ADMIN','STAFF') DEFAULT 'STAFF',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Users`
--

LOCK TABLES `Users` WRITE;
/*!40000 ALTER TABLE `Users` DISABLE KEYS */;
INSERT INTO `Users` VALUES
(1,'admin','123456','Super Admin','admin@library.com','ADMIN'),
(2,'staff01','123456','Library Staff 01','staff01@library.com','STAFF');
/*!40000 ALTER TABLE `Users` ENABLE KEYS */;
UNLOCK TABLES;
