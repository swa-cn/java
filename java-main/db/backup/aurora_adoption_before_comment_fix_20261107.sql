-- MySQL dump 10.13  Distrib 8.0.39, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: aurora
-- ------------------------------------------------------
-- Server version	8.0.39

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `pet`
--

DROP TABLE IF EXISTS `pet`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pet` (
  `id` varchar(64) NOT NULL COMMENT '??ID',
  `user_key` varchar(64) NOT NULL COMMENT '????',
  `species_id` varchar(32) NOT NULL COMMENT '??ID',
  `name` varchar(64) NOT NULL COMMENT '????',
  `status` varchar(16) NOT NULL DEFAULT 'egg' COMMENT 'egg=?, hatched=???',
  `intimacy` int NOT NULL DEFAULT '0' COMMENT '??? 0~100',
  `satiety` int NOT NULL DEFAULT '60' COMMENT '??? 0~100',
  `mood` int NOT NULL DEFAULT '80' COMMENT '?? 0~100',
  `created_at` bigint NOT NULL COMMENT '????',
  `hatched_at` bigint DEFAULT NULL COMMENT '????',
  `last_fed` bigint NOT NULL DEFAULT '0' COMMENT '???????',
  `last_interact` bigint NOT NULL DEFAULT '0' COMMENT '???????',
  PRIMARY KEY (`id`),
  KEY `idx_pet_user` (`user_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='????';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pet`
--

LOCK TABLES `pet` WRITE;
/*!40000 ALTER TABLE `pet` DISABLE KEYS */;
INSERT INTO `pet` VALUES ('p1a1b1f65c92245','eee','cat','???','hatched',17,60,90,1793987075218,1793987082135,0,1793987089704);
/*!40000 ALTER TABLE `pet` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pet_species`
--

DROP TABLE IF EXISTS `pet_species`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pet_species` (
  `id` varchar(32) NOT NULL COMMENT '??ID, ? cat/dog',
  `name` varchar(64) NOT NULL COMMENT '??',
  `emoji` varchar(8) NOT NULL COMMENT '??',
  `color` varchar(16) NOT NULL COMMENT '???',
  `description` varchar(255) DEFAULT '' COMMENT '??',
  `hatch_cost` int NOT NULL DEFAULT '20' COMMENT '??????',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '????',
  `created_at` bigint NOT NULL COMMENT '????',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='??????';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pet_species`
--

LOCK TABLES `pet_species` WRITE;
/*!40000 ALTER TABLE `pet_species` DISABLE KEYS */;
INSERT INTO `pet_species` VALUES ('bird','玄凤鹦鹉','🐦','#3ec4a8','黄冠腮红，吹口哨高手',20,5,1793985378000),('cat','布偶猫','🐱','#ff6fa8','蓝眼睛小天使，黏人又温柔',20,1,1793985378000),('dog','小柯基','🐶','#ff8a5c','蜜桃臀小短腿，快乐源泉',25,2,1793985378000),('fish','小金鱼','🐠','#4ab3e8','圆肚小福星，慢悠悠摆尾',15,6,1793985378000),('hamster','三线仓鼠','🐹','#ffb35c','腮帮子仓库，囤粮小能手',15,4,1793985378000),('rabbit','垂耳兔','🐰','#b98bff','长耳软萌，安静小可爱',20,3,1793985378000);
/*!40000 ALTER TABLE `pet_species` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pet_user_resource`
--

DROP TABLE IF EXISTS `pet_user_resource`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pet_user_resource` (
  `user_key` varchar(64) NOT NULL COMMENT '????(???)',
  `nutrients` int NOT NULL DEFAULT '0' COMMENT '??',
  `food` int NOT NULL DEFAULT '0' COMMENT '??',
  `updated_at` bigint NOT NULL COMMENT '????',
  PRIMARY KEY (`user_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='??????';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pet_user_resource`
--

LOCK TABLES `pet_user_resource` WRITE;
/*!40000 ALTER TABLE `pet_user_resource` DISABLE KEYS */;
INSERT INTO `pet_user_resource` VALUES ('eee',3,0,1793987082133);
/*!40000 ALTER TABLE `pet_user_resource` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pet_task_progress`
--

DROP TABLE IF EXISTS `pet_task_progress`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pet_task_progress` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_key` varchar(64) NOT NULL,
  `task_id` varchar(32) NOT NULL COMMENT '????ID',
  `task_date` varchar(10) NOT NULL COMMENT '?? yyyy-MM-dd',
  `completed` tinyint NOT NULL DEFAULT '0' COMMENT '?????',
  `claimed` tinyint NOT NULL DEFAULT '0' COMMENT '??????',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task` (`user_key`,`task_id`,`task_date`),
  KEY `idx_task_user` (`user_key`,`task_date`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='??????';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pet_task_progress`
--

LOCK TABLES `pet_task_progress` WRITE;
/*!40000 ALTER TABLE `pet_task_progress` DISABLE KEYS */;
INSERT INTO `pet_task_progress` VALUES (5,'eee','sign','2026-11-07',1,1),(6,'eee','read','2026-11-07',1,1),(7,'eee','quiz','2026-11-07',1,1),(8,'eee','walk','2026-11-07',1,0);
/*!40000 ALTER TABLE `pet_task_progress` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pet_interact_log`
--

DROP TABLE IF EXISTS `pet_interact_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pet_interact_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_key` varchar(64) NOT NULL,
  `interact_date` varchar(10) NOT NULL COMMENT '?? yyyy-MM-dd',
  `interact_count` int NOT NULL DEFAULT '0' COMMENT '??????',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_interact` (`user_key`,`interact_date`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='??????';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pet_interact_log`
--

LOCK TABLES `pet_interact_log` WRITE;
/*!40000 ALTER TABLE `pet_interact_log` DISABLE KEYS */;
INSERT INTO `pet_interact_log` VALUES (2,'eee','2026-11-07',5);
/*!40000 ALTER TABLE `pet_interact_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pet_gift_log`
--

DROP TABLE IF EXISTS `pet_gift_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pet_gift_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_key` varchar(64) NOT NULL COMMENT '???',
  `friend_key` varchar(64) NOT NULL COMMENT '???',
  `gift_date` varchar(10) NOT NULL COMMENT '?? yyyy-MM-dd',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_gift` (`user_key`,`friend_key`,`gift_date`),
  KEY `idx_gift_user` (`user_key`,`gift_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='??????';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pet_gift_log`
--

LOCK TABLES `pet_gift_log` WRITE;
/*!40000 ALTER TABLE `pet_gift_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `pet_gift_log` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-11-07  2:50:43
