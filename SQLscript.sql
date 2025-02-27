CREATE DATABASE IF NOT EXISTS `bilabonnement` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `bilabonnement`;

-- Drop and create tables in the correct order
DROP TABLE IF EXISTS `skaderapport`;
DROP TABLE IF EXISTS `lejeaftale`;
DROP TABLE IF EXISTS `nybil`;
DROP TABLE IF EXISTS `kunde`;
DROP TABLE IF EXISTS `bruger`;
DROP TABLE IF EXISTS `bil`;

CREATE TABLE `bil` (
  `chassisNumber` varchar(255) NOT NULL,
  `LicensePlate` varchar(255) DEFAULT NULL,
  `km` int DEFAULT NULL,
  `brand` varchar(255) DEFAULT NULL,
  `carModel` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `fuel` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`chassisNumber`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `bruger` (
  `Medarbejder_id` int NOT NULL AUTO_INCREMENT,
  `Login` varchar(255) NOT NULL,
  `Password` varchar(255) NOT NULL,
  `Type` varchar(255) NOT NULL,
  PRIMARY KEY (`Medarbejder_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `kunde` (
  `Kunde_id` int NOT NULL AUTO_INCREMENT,
  `Kunde_Navn` varchar(255) NOT NULL,
  `Telefon_nummer` varchar(15) NOT NULL,
  `Email` varchar(255) NOT NULL,
  `Adresse` varchar(255) NOT NULL,
  PRIMARY KEY (`Kunde_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `lejeaftale` (
  `Lejeaftale_id` int NOT NULL AUTO_INCREMENT,
  `chassisNumber` varchar(255) DEFAULT NULL,
  `dato` date NOT NULL,
  `Udlejnings_Type` varchar(255) NOT NULL,
  `Afhentningstidspunkt` time NOT NULL,
  `Afhentningssted` varchar(255) NOT NULL,
  `udlejningsperiode` int NOT NULL DEFAULT '0',
  `Kunde_id` int DEFAULT NULL,
  `status` varchar(255) NOT NULL,
  PRIMARY KEY (`Lejeaftale_id`),
  KEY `chassisNumber` (`chassisNumber`),
  KEY `Kunde_id` (`Kunde_id`),
  CONSTRAINT `lejeaftale_ibfk_1` FOREIGN KEY (`chassisNumber`) REFERENCES `bil` (`chassisNumber`),
  CONSTRAINT `lejeaftale_ibfk_2` FOREIGN KEY (`Kunde_id`) REFERENCES `kunde` (`Kunde_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `nybil` (
  `chassisNumber` varchar(255) NOT NULL,
  `LicensePlate` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`chassisNumber`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `skaderapport` (
  `Skade_id` int NOT NULL AUTO_INCREMENT,
  `Lejeaftale_id` int DEFAULT NULL,
  `Skade` varchar(255) DEFAULT NULL,
  `Skade_Pris` double DEFAULT NULL,
  `Kunde_id` int DEFAULT NULL,
  PRIMARY KEY (`Skade_id`),
  KEY `Lejeaftale_id` (`Lejeaftale_id`),
  KEY `Kunde_id` (`Kunde_id`),
  CONSTRAINT `skaderapport_ibfk_1` FOREIGN KEY (`Lejeaftale_id`) REFERENCES `lejeaftale` (`Lejeaftale_id`),
  CONSTRAINT `skaderapport_ibfk_2` FOREIGN KEY (`Kunde_id`) REFERENCES `kunde` (`Kunde_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Insert data in correct order
INSERT INTO `bil` VALUES 
('1FTSW21R08EB12345','LM89123',45000,'Ford','F-150','Pickup','Diesel','Udlejet'),
('1G1ZE5E75BF123456','VW34567',35000,'Chevrolet','Malibu','Sedan','Benzin','Ledig'),
('1HGCM82633A123456','HI34567',0,'Honda','Accord','Sedan','Benzin','Udlejet'),
('2GCEC19T2Y1123456','NO91234',60000,'Chevrolet','Silverado','Pickup','Benzin','Udlejet'),
('3FAHP0HA8AR123456','PQ34567',80000,'Ford','Fusion','Sedan','Hybrid','Ledig'),
('JN1CV6EK9FM202345','FG67890',0,'Nissan','Altima','Sedan','Benzin','Udlejet'),
('5YFBURHE7EP143166','TU23456',4000,'Toyota','Corolla','Hatchback','Benzin','Udlejet');

INSERT INTO `bruger` VALUES 
(1,'admin','admin2024','Administrator'),
(2,'data','data2024','Dataregistrer'),
(3,'skade','skade2024','SkadeOgUdbedring'),
(4,'forretningsudvikler','for2024','Forretningsudvikler');

INSERT INTO `kunde` VALUES 
(1,'Pernille Jensen','24012492','pj@get2net.dk','Frederiksberg Alle 4'),
(2,'Johannes Thomsen','14523562','johant@tdc.dk','Svalbardvej 52'),
(3,'Poul Christensen','80259525','pc@hotmail.dk','Amalienborg Slotsplads 5'),
(4,'Bjørn Larsen','82010505','blarsen@webmail.dk','J.C. Jacobsens Gade 1'),
(25,'Test Kunde 25','11112222','test25@example.com','Testvej 25'),
(32,'Test Kunde 32','33334444','test32@example.com','Testvej 32');

-- Now insert into `lejeaftale`
INSERT INTO `lejeaftale` VALUES 
(1,'JN1CV6EK9FM202345','2024-06-11','Limited','12:00:00','Værksted Aalborg',5,1,'Afventende'),
(2,'1HGCM82633A123456','2024-06-15','Unlimited','16:00:00','Værksted Aarhus',12,2,'Afventende'),
(3,'1FTSW21R08EB12345','2024-06-15','Unlimited','16:00:00','Værksted Odense',25,2,'Afventende'),
(4,'2GCEC19T2Y1123456','2024-05-01','Unlimited','16:00:00','Værksted Aalborg',25,2,'Udlejet'),
(5,'5YFBURHE7EP143166','2024-05-01','Limited','16:00:00','Værksted KBH',32,2,'Udlejet');

INSERT INTO `nybil` VALUES ('a303','ab12345');

-- Insert into skaderapport last (since it references lejeaftale and kunde)
INSERT INTO `skaderapport` VALUES 
(1,1,'Ridser på venstre dør',1500,1),
(2,2,'Forrude revnet',3000,2);
