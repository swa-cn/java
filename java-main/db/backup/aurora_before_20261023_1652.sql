-- MySQL dump 10.13  Distrib 8.0.39, for Win64 (x86_64)
--
-- Host: localhost    Database: aurora
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
-- Current Database: `aurora`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `aurora` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `aurora`;

--
-- Table structure for table `app_user`
--

DROP TABLE IF EXISTS `app_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `app_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(20) NOT NULL,
  `email` varchar(120) DEFAULT NULL,
  `nickname` varchar(24) DEFAULT NULL,
  `password` varchar(100) NOT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_user`
--

LOCK TABLES `app_user` WRITE;
/*!40000 ALTER TABLE `app_user` DISABLE KEYS */;
INSERT INTO `app_user` VALUES (3,'eee',NULL,'33','$2a$10$Uo3ffo2bxxc1HqlHBjCcQ.3gGr9VugtbjMSa5XvM8Graz8qidhuga','2026-10-23 16:47:50','2026-10-23 16:47:50'),(4,'rrr',NULL,'44','$2a$10$EP.pKn.oJKHW.Ryvs4TpvufR4UDpVsFlPAIBPW29B8gc6o0aRcAJG','2026-10-23 16:48:40','2026-10-23 16:48:40');
/*!40000 ALTER TABLE `app_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pet_breed`
--

DROP TABLE IF EXISTS `pet_breed`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pet_breed` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `category_id` varchar(20) NOT NULL,
  `name` varchar(40) NOT NULL,
  `image` varchar(300) DEFAULT NULL,
  `description` varchar(200) DEFAULT NULL,
  `sort_no` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_category_sort` (`category_id`,`sort_no`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pet_breed`
--

LOCK TABLES `pet_breed` WRITE;
/*!40000 ALTER TABLE `pet_breed` DISABLE KEYS */;
INSERT INTO `pet_breed` VALUES (1,'cat','布偶猫','/images/pets/cat.png','温顺黏人，蓝眼睛像小天使',1),(2,'cat','英国短毛猫','/images/pets/cat_british.png','圆脸胖墩，慵懒小可爱',2),(3,'cat','缅因猫','/images/pets/cat_maine.png','体型巨大，温柔的毛毛怪',3),(4,'cat','橘猫','/images/pets/cat_orange.png','十橘九胖，干饭第一名',4),(5,'cat','暹罗猫','/images/pets/cat_siamese.png','话痨小煤球，超爱撒娇',5),(6,'dog','柯基','/images/pets/dog.png','蜜桃臀小短腿，快乐源泉',1),(7,'dog','金毛','/images/pets/dog_golden.png','暖男代表，温柔大暖炉',2),(8,'dog','柴犬','/images/pets/dog_shiba.png','表情包担当，倔强又可爱',3),(9,'dog','哈士奇','/images/pets/dog_husky.png','行走的拆迁队，二哈本哈',4),(10,'dog','边牧','/images/pets/dog_border.png','狗界智商天花板，运动健将',5),(11,'small','仓鼠','/images/pets/hamster.png','腮帮子仓库，囤粮小能手',1),(12,'small','龙猫','/images/pets/small_chinchilla.png','软糯棉花糖，怕热小公主',2),(13,'small','兔子','/images/pets/small_rabbit.png','长耳吃货，安静又软萌',3),(14,'small','荷兰猪','/images/pets/small_guinea.png','咕咕叫的小胖墩，超亲人',4),(15,'bird','鹦鹉','/images/pets/bird.png','模仿达人，话痨小歌手',1),(16,'bird','文鸟','/images/pets/bird_bunting.png','圆滚滚小白团，安静温顺',2),(17,'bird','玄凤','/images/pets/bird_cockatiel.png','黄冠腮红，吹口哨高手',3),(18,'bird','鸽子','/images/pets/bird_pigeon.png','和平使者，咕咕小邮差',4),(19,'fish','金鱼','/images/pets/fish.png','圆肚小福星，慢悠悠摆尾',1),(20,'fish','锦鲤','/images/pets/fish_koi.png','好运象征，池中花团锦簇',2),(21,'fish','斗鱼','/images/pets/fish_betta.png','飘逸长鳍，孤傲小武士',3),(22,'fish','热带鱼','/images/pets/fish_tropical.png','色彩斑斓，群游小精灵',4);
/*!40000 ALTER TABLE `pet_breed` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pet_category`
--

DROP TABLE IF EXISTS `pet_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pet_category` (
  `id` varchar(20) NOT NULL,
  `name` varchar(40) NOT NULL,
  `icon` varchar(10) DEFAULT NULL,
  `color` varchar(20) DEFAULT NULL,
  `image` varchar(300) DEFAULT NULL,
  `sort_no` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_sort` (`sort_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pet_category`
--

LOCK TABLES `pet_category` WRITE;
/*!40000 ALTER TABLE `pet_category` DISABLE KEYS */;
INSERT INTO `pet_category` VALUES ('bird','鸟类','啾','#3ec4a8','/images/pets/bird.png',4),('cat','猫类','喵','#ff6fa8','/images/pets/cat.png',1),('dog','狗类','汪','#ff8a5c','/images/pets/dog.png',2),('fish','水族','鱼','#4ab3e8','/images/pets/fish.png',5),('small','小宠','啮','#8b7bff','/images/pets/hamster.png',3);
/*!40000 ALTER TABLE `pet_category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pet_friend`
--

DROP TABLE IF EXISTS `pet_friend`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pet_friend` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `owner_key` varchar(20) NOT NULL,
  `friend_key` varchar(20) NOT NULL,
  `friend_name` varchar(24) DEFAULT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'pending',
  `source` varchar(16) DEFAULT 'manual',
  `created_at` datetime DEFAULT NULL,
  `accepted_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_owner_friend` (`owner_key`,`friend_key`),
  KEY `idx_owner_status` (`owner_key`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pet_friend`
--

LOCK TABLES `pet_friend` WRITE;
/*!40000 ALTER TABLE `pet_friend` DISABLE KEYS */;
INSERT INTO `pet_friend` VALUES (1,'cutetest01','cutetest02','测试02','friend','manual','2026-10-23 16:42:42','2026-10-23 16:42:42'),(2,'cutetest02','cutetest01','cutetest01','friend','manual','2026-10-23 16:42:42','2026-10-23 16:42:42'),(3,'rrr','eee','33','friend','post','2026-10-23 16:48:47','2026-10-23 16:49:03'),(4,'eee','rrr','44','friend','post','2026-10-23 16:49:03','2026-10-23 16:49:03');
/*!40000 ALTER TABLE `pet_friend` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pet_message`
--

DROP TABLE IF EXISTS `pet_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pet_message` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `owner_key` varchar(20) NOT NULL,
  `peer_key` varchar(20) NOT NULL,
  `peer_name` varchar(24) DEFAULT NULL,
  `from_key` varchar(20) NOT NULL,
  `from_name` varchar(24) DEFAULT NULL,
  `content` varchar(500) NOT NULL,
  `read_flag` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_owner_peer` (`owner_key`,`peer_key`),
  KEY `idx_owner_created` (`owner_key`,`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pet_message`
--

LOCK TABLES `pet_message` WRITE;
/*!40000 ALTER TABLE `pet_message` DISABLE KEYS */;
INSERT INTO `pet_message` VALUES (1,'cutetest01','cutetest02','测试02','cutetest01','cutetest01','你好呀 cutetest02!',1,'2026-10-23 16:42:42'),(2,'cutetest02','cutetest01','cutetest01','cutetest01','cutetest01','你好呀 cutetest02!',1,'2026-10-23 16:42:42');
/*!40000 ALTER TABLE `pet_message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pet_post`
--

DROP TABLE IF EXISTS `pet_post`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pet_post` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(80) DEFAULT NULL,
  `content` text,
  `category` varchar(20) NOT NULL DEFAULT 'cat',
  `tags` varchar(200) DEFAULT NULL,
  `author` varchar(20) NOT NULL,
  `author_name` varchar(24) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_created` (`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pet_post`
--

LOCK TABLES `pet_post` WRITE;
/*!40000 ALTER TABLE `pet_post` DISABLE KEYS */;
INSERT INTO `pet_post` VALUES (1,'1','1','cat','1','eee','33','2026-10-23 16:48:21');
/*!40000 ALTER TABLE `pet_post` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pet_post_comment`
--

DROP TABLE IF EXISTS `pet_post_comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pet_post_comment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL,
  `author` varchar(20) NOT NULL,
  `author_name` varchar(24) DEFAULT NULL,
  `content` varchar(300) NOT NULL,
  `created_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_post` (`post_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pet_post_comment`
--

LOCK TABLES `pet_post_comment` WRITE;
/*!40000 ALTER TABLE `pet_post_comment` DISABLE KEYS */;
INSERT INTO `pet_post_comment` VALUES (1,1,'rrr','44','1','2026-10-23 16:48:52');
/*!40000 ALTER TABLE `pet_post_comment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pet_post_like`
--

DROP TABLE IF EXISTS `pet_post_like`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pet_post_like` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL,
  `user_key` varchar(20) NOT NULL,
  `created_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_user` (`post_id`,`user_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pet_post_like`
--

LOCK TABLES `pet_post_like` WRITE;
/*!40000 ALTER TABLE `pet_post_like` DISABLE KEYS */;
/*!40000 ALTER TABLE `pet_post_like` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pet_post_media`
--

DROP TABLE IF EXISTS `pet_post_media`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pet_post_media` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL,
  `media_type` varchar(10) NOT NULL,
  `url` varchar(300) NOT NULL,
  `name` varchar(200) DEFAULT NULL,
  `size_bytes` bigint DEFAULT NULL,
  `sort_no` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_post` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pet_post_media`
--

LOCK TABLES `pet_post_media` WRITE;
/*!40000 ALTER TABLE `pet_post_media` DISABLE KEYS */;
/*!40000 ALTER TABLE `pet_post_media` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-23 16:53:23
