/*
 Navicat Premium Dump SQL

 Source Server         : test
 Source Server Type    : MySQL
 Source Server Version : 80039 (8.0.39)
 Source Host           : localhost:3306
 Source Schema         : aurora

 Target Server Type    : MySQL
 Target Server Version : 80039 (8.0.39)
 File Encoding         : 65001

 Date: 16/10/2026 16:49:05
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for app_user
-- ----------------------------
DROP TABLE IF EXISTS `app_user`;
CREATE TABLE `app_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `email` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `nickname` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `created_at` datetime NULL DEFAULT NULL,
  `updated_at` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `uk_email`(`email` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of app_user
-- ----------------------------
INSERT INTO `app_user` VALUES (1, 'qqq', NULL, '11', '$2a$10$oNFN3A7o3VaZsHma0f.5jO3uAKkH2qrByZkpxKZ0PDpRvXOwiNc3m', '2026-10-16 15:40:26', '2026-10-16 15:40:26');
INSERT INTO `app_user` VALUES (2, 'www', NULL, '22', '$2a$10$GbRWFYvGDqr.ZdemvgfsaejVxQQQ61LZ2KLrheiMKed44FQKvCmsy', '2026-10-16 16:28:21', '2026-10-16 16:28:21');

-- ----------------------------
-- Table structure for pet_breed
-- ----------------------------
DROP TABLE IF EXISTS `pet_breed`;
CREATE TABLE `pet_breed`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `category_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '鎵?睘鍒嗙被 id',
  `name` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '鍝佺?鍚',
  `image` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鍝佺?鍥',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鍝佺?绠?粙',
  `sort_no` int NOT NULL DEFAULT 0 COMMENT '鎺掑簭锛堝崌搴忥級',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_category_sort`(`category_id` ASC, `sort_no` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '钀屽疇鍝佺?鍥鹃壌' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pet_breed
-- ----------------------------
INSERT INTO `pet_breed` VALUES (1, 'cat', '布偶猫', '/images/pets/cat.png', '温顺黏人，蓝眼睛像小天使', 1);
INSERT INTO `pet_breed` VALUES (2, 'cat', '英国短毛猫', '/images/pets/cat_british.png', '圆脸胖墩，慵懒小可爱', 2);
INSERT INTO `pet_breed` VALUES (3, 'cat', '缅因猫', '/images/pets/cat_maine.png', '体型巨大，温柔的毛毛怪', 3);
INSERT INTO `pet_breed` VALUES (4, 'cat', '橘猫', '/images/pets/cat_orange.png', '十橘九胖，干饭第一名', 4);
INSERT INTO `pet_breed` VALUES (5, 'cat', '暹罗猫', '/images/pets/cat_siamese.png', '话痨小煤球，超爱撒娇', 5);
INSERT INTO `pet_breed` VALUES (6, 'dog', '柯基', '/images/pets/dog.png', '蜜桃臀小短腿，快乐源泉', 1);
INSERT INTO `pet_breed` VALUES (7, 'dog', '金毛', '/images/pets/dog_golden.png', '暖男代表，温柔大暖炉', 2);
INSERT INTO `pet_breed` VALUES (8, 'dog', '柴犬', '/images/pets/dog_shiba.png', '表情包担当，倔强又可爱', 3);
INSERT INTO `pet_breed` VALUES (9, 'dog', '哈士奇', '/images/pets/dog_husky.png', '行走的拆迁队，二哈本哈', 4);
INSERT INTO `pet_breed` VALUES (10, 'dog', '边牧', '/images/pets/dog_border.png', '狗界智商天花板，运动健将', 5);
INSERT INTO `pet_breed` VALUES (11, 'small', '仓鼠', '/images/pets/hamster.png', '腮帮子仓库，囤粮小能手', 1);
INSERT INTO `pet_breed` VALUES (12, 'small', '龙猫', '/images/pets/small_chinchilla.png', '软糯棉花糖，怕热小公主', 2);
INSERT INTO `pet_breed` VALUES (13, 'small', '兔子', '/images/pets/small_rabbit.png', '长耳吃货，安静又软萌', 3);
INSERT INTO `pet_breed` VALUES (14, 'small', '荷兰猪', '/images/pets/small_guinea.png', '咕咕叫的小胖墩，超亲人', 4);
INSERT INTO `pet_breed` VALUES (15, 'bird', '鹦鹉', '/images/pets/bird.png', '模仿达人，话痨小歌手', 1);
INSERT INTO `pet_breed` VALUES (16, 'bird', '文鸟', '/images/pets/bird_bunting.png', '圆滚滚小白团，安静温顺', 2);
INSERT INTO `pet_breed` VALUES (17, 'bird', '玄凤', '/images/pets/bird_cockatiel.png', '黄冠腮红，吹口哨高手', 3);
INSERT INTO `pet_breed` VALUES (18, 'bird', '鸽子', '/images/pets/bird_pigeon.png', '和平使者，咕咕小邮差', 4);
INSERT INTO `pet_breed` VALUES (19, 'fish', '金鱼', '/images/pets/fish.png', '圆肚小福星，慢悠悠摆尾', 1);
INSERT INTO `pet_breed` VALUES (20, 'fish', '锦鲤', '/images/pets/fish_koi.png', '好运象征，池中花团锦簇', 2);
INSERT INTO `pet_breed` VALUES (21, 'fish', '斗鱼', '/images/pets/fish_betta.png', '飘逸长鳍，孤傲小武士', 3);
INSERT INTO `pet_breed` VALUES (22, 'fish', '热带鱼', '/images/pets/fish_tropical.png', '色彩斑斓，群游小精灵', 4);

-- ----------------------------
-- Table structure for pet_category
-- ----------------------------
DROP TABLE IF EXISTS `pet_category`;
CREATE TABLE `pet_category`  (
  `id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '鍒嗙被 id锛歝at/dog/small/bird/fish',
  `name` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '鍒嗙被鍚',
  `icon` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鍗曞瓧鍥炬爣',
  `color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '涓婚?鑹',
  `image` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '灏侀潰鍥',
  `sort_no` int NOT NULL DEFAULT 0 COMMENT '鎺掑簭锛堝崌搴忥級',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sort`(`sort_no` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '钀屽疇鍒嗙被' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pet_category
-- ----------------------------
INSERT INTO `pet_category` VALUES ('bird', '鸟类', '啾', '#3ec4a8', '/images/pets/bird.png', 4);
INSERT INTO `pet_category` VALUES ('cat', '猫类', '喵', '#ff6fa8', '/images/pets/cat.png', 1);
INSERT INTO `pet_category` VALUES ('dog', '狗类', '汪', '#ff8a5c', '/images/pets/dog.png', 2);
INSERT INTO `pet_category` VALUES ('fish', '水族', '鱼', '#4ab3e8', '/images/pets/fish.png', 5);
INSERT INTO `pet_category` VALUES ('small', '小宠', '啮', '#8b7bff', '/images/pets/hamster.png', 3);

-- ----------------------------
-- Table structure for pet_friend
-- ----------------------------
DROP TABLE IF EXISTS `pet_friend`;
CREATE TABLE `pet_friend`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `owner_key` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `friend_key` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `friend_name` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'pending',
  `source` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'manual',
  `created_at` datetime NULL DEFAULT NULL,
  `accepted_at` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_owner_friend`(`owner_key` ASC, `friend_key` ASC) USING BTREE,
  INDEX `idx_owner_status`(`owner_key` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pet_friend
-- ----------------------------
INSERT INTO `pet_friend` VALUES (5, 'www', 'qqq', '11', 'friend', 'post', '2026-10-16 16:42:21', '2026-10-16 16:44:28');
INSERT INTO `pet_friend` VALUES (6, 'qqq', 'www', '22', 'friend', 'post', '2026-10-16 16:44:28', '2026-10-16 16:44:28');

-- ----------------------------
-- Table structure for pet_message
-- ----------------------------
DROP TABLE IF EXISTS `pet_message`;
CREATE TABLE `pet_message`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `owner_key` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `peer_key` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `peer_name` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `from_key` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `from_name` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `read_flag` tinyint(1) NOT NULL DEFAULT 1,
  `created_at` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_owner_peer`(`owner_key` ASC, `peer_key` ASC) USING BTREE,
  INDEX `idx_owner_created`(`owner_key` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pet_message
-- ----------------------------
INSERT INTO `pet_message` VALUES (3, 'qqq', 'www', '22', 'qqq', '11', 'nh', 1, '2026-10-16 16:44:36');
INSERT INTO `pet_message` VALUES (4, 'www', 'qqq', '11', 'qqq', '11', 'nh', 1, '2026-10-16 16:44:36');

-- ----------------------------
-- Table structure for pet_post
-- ----------------------------
DROP TABLE IF EXISTS `pet_post`;
CREATE TABLE `pet_post`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `category` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'cat',
  `tags` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `author` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `author_name` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_category`(`category` ASC) USING BTREE,
  INDEX `idx_created`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pet_post
-- ----------------------------
INSERT INTO `pet_post` VALUES (1, '11', '11w', 'cat', 'ds', 'qqq', '11', '2026-10-16 16:17:29');

-- ----------------------------
-- Table structure for pet_post_comment
-- ----------------------------
DROP TABLE IF EXISTS `pet_post_comment`;
CREATE TABLE `pet_post_comment`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL,
  `author` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `author_name` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `content` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `created_at` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_post`(`post_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pet_post_comment
-- ----------------------------
INSERT INTO `pet_post_comment` VALUES (1, 1, 'www', '22', 'hhhhh', '2026-10-16 16:31:24');

-- ----------------------------
-- Table structure for pet_post_like
-- ----------------------------
DROP TABLE IF EXISTS `pet_post_like`;
CREATE TABLE `pet_post_like`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL,
  `user_key` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `created_at` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_post_user`(`post_id` ASC, `user_key` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pet_post_like
-- ----------------------------
INSERT INTO `pet_post_like` VALUES (1, 1, 'qqq', '2026-10-16 16:17:32');
INSERT INTO `pet_post_like` VALUES (2, 1, 'www', '2026-10-16 16:31:26');

-- ----------------------------
-- Table structure for pet_post_media
-- ----------------------------
DROP TABLE IF EXISTS `pet_post_media`;
CREATE TABLE `pet_post_media`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL,
  `media_type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `url` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `size_bytes` bigint NULL DEFAULT NULL,
  `sort_no` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_post`(`post_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pet_post_media
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
