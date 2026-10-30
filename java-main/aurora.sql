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

 Date: 30/10/2026 16:55:31
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
  `role` tinyint NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `uk_email`(`email` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of app_user
-- ----------------------------
INSERT INTO `app_user` VALUES (3, 'eee', NULL, '33', 'eee123456', '2026-10-23 16:47:50', '2026-10-23 16:47:50', 1);
INSERT INTO `app_user` VALUES (4, 'rrr', NULL, '44', 'rrr123456', '2026-10-23 16:48:40', '2026-10-23 16:48:40', 1);
INSERT INTO `app_user` VALUES (5, 'admin', NULL, '管理员', 'admin123456', '2026-10-26 11:04:18', '2026-10-26 11:04:18', 0);
INSERT INTO `app_user` VALUES (6, 'qqq', NULL, '11', 'qqq123456', '2026-10-28 08:58:05', '2026-10-28 08:58:05', 0);

-- ----------------------------
-- Table structure for pet_breed
-- ----------------------------
DROP TABLE IF EXISTS `pet_breed`;
CREATE TABLE `pet_breed`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `category_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `name` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `image` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `sort_no` int NOT NULL DEFAULT 0,
  `temperament` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `care_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `suitable_for` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_category_sort`(`category_id` ASC, `sort_no` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pet_breed
-- ----------------------------
INSERT INTO `pet_breed` VALUES (1, 'cat', '布偶猫', '/images/pets/cat.png', '温顺黏人，蓝眼睛像小天使', 1, NULL, NULL, NULL);
INSERT INTO `pet_breed` VALUES (2, 'cat', '英国短毛猫', '/images/pets/cat_british.png', '圆脸胖墩，慵懒小可爱', 2, NULL, NULL, NULL);
INSERT INTO `pet_breed` VALUES (3, 'cat', '缅因猫', '/images/pets/cat_maine.png', '体型巨大，温柔的毛毛怪', 3, NULL, NULL, NULL);
INSERT INTO `pet_breed` VALUES (4, 'cat', '橘猫', '/images/pets/cat_orange.png', '十橘九胖，干饭第一名', 4, NULL, NULL, NULL);
INSERT INTO `pet_breed` VALUES (5, 'cat', '暹罗猫', '/images/pets/cat_siamese.png', '话痨小煤球，超爱撒娇', 5, NULL, NULL, NULL);
INSERT INTO `pet_breed` VALUES (6, 'dog', '柯基', '/images/pets/dog.png', '蜜桃臀小短腿，快乐源泉', 1, NULL, NULL, NULL);
INSERT INTO `pet_breed` VALUES (7, 'dog', '金毛', '/images/pets/dog_golden.png', '暖男代表，温柔大暖炉', 2, NULL, NULL, NULL);
INSERT INTO `pet_breed` VALUES (8, 'dog', '柴犬', '/images/pets/dog_shiba.png', '表情包担当，倔强又可爱', 3, NULL, NULL, NULL);
INSERT INTO `pet_breed` VALUES (9, 'dog', '哈士奇', '/images/pets/dog_husky.png', '行走的拆迁队，二哈本哈', 4, NULL, NULL, NULL);
INSERT INTO `pet_breed` VALUES (10, 'dog', '边牧', '/images/pets/dog_border.png', '狗界智商天花板，运动健将', 5, NULL, NULL, NULL);
INSERT INTO `pet_breed` VALUES (11, 'small', '仓鼠', '/images/pets/hamster.png', '腮帮子仓库，囤粮小能手', 1, NULL, NULL, NULL);
INSERT INTO `pet_breed` VALUES (12, 'small', '龙猫', '/images/pets/small_chinchilla.png', '软糯棉花糖，怕热小公主', 2, NULL, NULL, NULL);
INSERT INTO `pet_breed` VALUES (13, 'small', '兔子', '/images/pets/small_rabbit.png', '长耳吃货，安静又软萌', 3, NULL, NULL, NULL);
INSERT INTO `pet_breed` VALUES (14, 'small', '荷兰猪', '/images/pets/small_guinea.png', '咕咕叫的小胖墩，超亲人', 4, NULL, NULL, NULL);
INSERT INTO `pet_breed` VALUES (15, 'bird', '鹦鹉', '/images/pets/bird.png', '模仿达人，话痨小歌手', 1, NULL, NULL, NULL);
INSERT INTO `pet_breed` VALUES (16, 'bird', '文鸟', '/images/pets/bird_bunting.png', '圆滚滚小白团，安静温顺', 2, NULL, NULL, NULL);
INSERT INTO `pet_breed` VALUES (17, 'bird', '玄凤', '/images/pets/bird_cockatiel.png', '黄冠腮红，吹口哨高手', 3, NULL, NULL, NULL);
INSERT INTO `pet_breed` VALUES (18, 'bird', '鸽子', '/images/pets/bird_pigeon.png', '和平使者，咕咕小邮差', 4, NULL, NULL, NULL);
INSERT INTO `pet_breed` VALUES (19, 'fish', '金鱼', '/images/pets/fish.png', '圆肚小福星，慢悠悠摆尾', 1, NULL, NULL, NULL);
INSERT INTO `pet_breed` VALUES (20, 'fish', '锦鲤', '/images/pets/fish_koi.png', '好运象征，池中花团锦簇', 2, NULL, NULL, NULL);
INSERT INTO `pet_breed` VALUES (21, 'fish', '斗鱼', '/images/pets/fish_betta.png', '飘逸长鳍，孤傲小武士', 3, NULL, NULL, NULL);
INSERT INTO `pet_breed` VALUES (22, 'fish', '热带鱼', '/images/pets/fish_tropical.png', '色彩斑斓，群游小精灵', 4, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for pet_category
-- ----------------------------
DROP TABLE IF EXISTS `pet_category`;
CREATE TABLE `pet_category`  (
  `id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `name` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `icon` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `image` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `sort_no` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sort`(`sort_no` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pet_friend
-- ----------------------------
INSERT INTO `pet_friend` VALUES (1, 'cutetest01', 'cutetest02', '测试02', 'friend', 'manual', '2026-10-23 16:42:42', '2026-10-23 16:42:42');
INSERT INTO `pet_friend` VALUES (2, 'cutetest02', 'cutetest01', 'cutetest01', 'friend', 'manual', '2026-10-23 16:42:42', '2026-10-23 16:42:42');
INSERT INTO `pet_friend` VALUES (3, 'rrr', 'eee', '33', 'friend', 'post', '2026-10-23 16:48:47', '2026-10-23 16:49:03');
INSERT INTO `pet_friend` VALUES (4, 'eee', 'rrr', '44', 'friend', 'post', '2026-10-23 16:49:03', '2026-10-23 16:49:03');
INSERT INTO `pet_friend` VALUES (5, 'admin', 'rrr', '44', 'pending', 'comment', '2026-10-28 08:56:52', NULL);
INSERT INTO `pet_friend` VALUES (6, 'admin', 'eee', '33', 'friend', 'post', '2026-10-28 08:56:54', '2026-10-28 08:59:53');
INSERT INTO `pet_friend` VALUES (7, 'qqq', 'eee', '33', 'friend', 'post', '2026-10-28 08:59:34', '2026-10-28 08:59:53');
INSERT INTO `pet_friend` VALUES (8, 'qqq', 'rrr', '44', 'pending', 'comment', '2026-10-28 08:59:34', NULL);
INSERT INTO `pet_friend` VALUES (9, 'eee', 'qqq', '11', 'friend', 'post', '2026-10-28 08:59:53', '2026-10-28 08:59:53');
INSERT INTO `pet_friend` VALUES (10, 'eee', 'admin', '管理员', 'friend', 'post', '2026-10-28 08:59:53', '2026-10-28 08:59:53');

-- ----------------------------
-- Table structure for pet_knowledge
-- ----------------------------
DROP TABLE IF EXISTS `pet_knowledge`;
CREATE TABLE `pet_knowledge`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cat` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'all',
  `title` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `summary` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `tags` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `content` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `views` bigint NOT NULL DEFAULT 0,
  `created_at` datetime NULL DEFAULT NULL,
  `updated_at` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_cat`(`cat` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pet_knowledge
-- ----------------------------
INSERT INTO `pet_knowledge` VALUES (1, 'cat', '新手养猫入门清单', '接猫回家前该准备什么、第一周怎么过渡，一篇说清。', '[\"新手\",\"准备\",\"用品\"]', '[{\"h\":\"接猫前的准备\",\"p\":\"提前备好猫粮（先问原主人喂的是什么牌子，避免突然换粮导致腹泻）、猫砂盆、猫砂、食盆水盆、猫抓板和一个可以让猫躲起来的半封闭窝。房间收好电线、百合花（对猫剧毒）和小零件。\"},{\"h\":\"到家第一周\",\"p\":\"把猫放到一个安静的小房间里，打开航空箱让它自己出来，不要强行抱或追。前几天只在这一个房间活动，吃喝如厕正常、开始主动探索后再逐步开放其他区域。\"},{\"h\":\"换粮要慢\",\"p\":\"换粮用 7 天过渡法：新粮比例从 25% 逐步加到 100%，混着喂，肠胃最不容易出问题。\"}]', 7, '2026-10-28 09:45:49', '2026-10-30 16:01:43');
INSERT INTO `pet_knowledge` VALUES (2, 'cat', '猫咪疫苗时间表', '猫三联、狂犬疫苗什么时候打、要不要每年补一针。', '[\"疫苗\",\"健康\",\"幼猫\"]', '[{\"h\":\"基础免疫\",\"p\":\"幼猫 8-9 周龄打第一针猫三联（防猫瘟、杯状病毒、疱疹病毒），之后每隔 3-4 周各补一针，共 2-3 针；最后一针在 16 周龄以后打完。狂犬疫苗一般 12 周龄后接种一针。\"},{\"h\":\"加强针\",\"p\":\"成年猫之后每 1-3 年加强一次，具体听兽医根据抗体水平和生活方式评估，室内猫和散养猫的方案不一样。\"},{\"h\":\"注意事项\",\"p\":\"疫苗要在健康状态下接种，感冒、拉肚子期间先治病。打完针留院观察 20-30 分钟再走。\"}]', 4, '2026-10-28 09:45:49', '2026-10-30 15:58:15');
INSERT INTO `pet_knowledge` VALUES (3, 'cat', '猫咪驱虫这样做', '体内体外、频率多少、用什么药一次讲明白。', '[\"驱虫\",\"健康\"]', '[{\"h\":\"体外驱虫（跳蚤、虱子）\",\"p\":\"滴剂拨开后颈毛发滴在皮肤上，猫舔不到的位置。频率一般每月一次；纯室内猫且环境干净可以放宽到 1-2 个月一次。\"},{\"h\":\"体内驱虫（蛔虫、绦虫等）\",\"p\":\"幼猫 6、8、12 周龄各驱一次，之后每 3 个月一次；喂生骨肉或爱捕猎的猫要更勤。\"},{\"h\":\"用药注意\",\"p\":\"认准猫用驱虫药，犬用药物里的某些成分（如氯菊酯）对猫剧毒。体重决定剂量，用药前称一下体重。\"}]', 2, '2026-10-28 09:45:49', '2026-10-30 15:58:16');
INSERT INTO `pet_knowledge` VALUES (4, 'cat', '猫咪喂食与饮食禁忌', '喂多少次、喝多少水，以及那些绝对不能碰的东西。', '[\"喂食\",\"禁忌\",\"饮水\"]', '[{\"h\":\"喂食频率\",\"p\":\"幼猫（2-6 月龄）一天 3-4 顿少食多餐；成年猫一天 2-3 顿即可。定量定时比自由采食更容易监控食欲变化。\"},{\"h\":\"喝水很重要\",\"p\":\"猫天生不爱喝水，慢性缺水容易伤肾和泌尿系统。多放几个水碗、用水循环泵或喂湿粮罐头都能明显增加饮水量。\"},{\"h\":\"危险食物清单\",\"p\":\"巧克力、洋葱、大蒜、葡萄/葡萄干、木糖醇、酒精、生鸡蛋清都不能喂；百合花全株对猫剧毒，养猫家里不要出现。牛奶多数猫乳糖不耐受，喝了容易腹泻。\"}]', 2, '2026-10-28 09:45:49', '2026-10-30 15:58:18');
INSERT INTO `pet_knowledge` VALUES (5, 'dog', '狗狗社会化黄金期', '3-14 周龄是性格塑造的关键窗口，错过很难补。', '[\"训练\",\"幼犬\",\"社会化\"]', '[{\"h\":\"什么是社会化\",\"p\":\"让幼犬在 3-14 周龄的敏感期里，安全地接触各种各样的人（戴帽子、打伞、小孩）、声音（吸尘器、雷声）、车辆和其他友善的狗，形成「这些都没什么大不了」的经验。\"},{\"h\":\"怎么做\",\"p\":\"每次接触保持短、愉快、可退出。幼犬主动想探索就鼓励，害怕就拉开距离，绝不硬塞。疫苗没打齐前可以抱着出门看世界，不落地也有用。\"},{\"h\":\"错过会怎样\",\"p\":\"缺乏社会化的狗成年后更容易胆小、敏感、对陌生人和狗过度反应，后天改善需要花数倍的时间。\"}]', 1, '2026-10-28 09:45:49', '2026-10-28 09:47:17');
INSERT INTO `pet_knowledge` VALUES (6, 'dog', '狗狗绝对不能吃的食物', '很多人食对狗有毒，这份黑名单请背下来。', '[\"食物\",\"禁忌\",\"安全\"]', '[{\"h\":\"高危食物\",\"p\":\"巧克力和咖啡因（可可碱中毒）、葡萄和葡萄干（急性肾衰）、木糖醇（口香糖、部分花生酱，血糖骤降）、洋葱大蒜（溶血）、酒精、夏威夷果。\"},{\"h\":\"容易被忽视的\",\"p\":\"煮熟的鸡鸭骨头会碎裂成尖刺，容易划伤食道；玉米芯、果核有噎住和梗阻风险；高盐高油的剩饭菜长期吃会伤胰腺和肾脏。\"},{\"h\":\"误食了怎么办\",\"p\":\"记下吃了什么、大概多少量、什么时候吃的，立刻联系宠物医院，2 小时内催吐效果最好，不要自己乱喂家庭药品。\"}]', 1, '2026-10-28 09:45:49', '2026-10-30 16:49:28');
INSERT INTO `pet_knowledge` VALUES (7, 'dog', '牵绳散步礼仪', '不爆冲、不捡食、会随行，散步才能人狗都开心。', '[\"散步\",\"训练\",\"牵引\"]', '[{\"h\":\"牵引是底线\",\"p\":\"市区一律牵绳：防车祸、防跑丢、防冲突。选 1.5-2 米的固定绳，伸缩绳不利于训练随行。\"},{\"h\":\"爆冲纠正\",\"p\":\"狗拉紧绳子人就停住，绳子松了才继续走，让狗明白「拉紧=走不了」。几周就能明显改善，配合零食奖励看你的位置。\"},{\"h\":\"捡食问题\",\"p\":\"走「S」路线绕开食物残渣，用「留下」口令加零食交换练习。散步前先喂七分饱，捡食动机也会下降。\"}]', 0, '2026-10-28 09:45:49', '2026-10-28 09:45:49');
INSERT INTO `pet_knowledge` VALUES (8, 'small', '仓鼠饲养要点', '必须单笼养、跑轮要够大，这些常识很多新手不知道。', '[\"仓鼠\",\"小宠\",\"笼子\"]', '[{\"h\":\"一鼠一笼\",\"p\":\"仓鼠是独居动物，合笼必打架，尤其叙利亚仓鼠（金丝熊）成年后同性也会互咬到重伤，务必一鼠一笼。\"},{\"h\":\"跑轮尺寸\",\"p\":\"跑轮太小会让仓鼠弓背奔跑，长期伤脊椎：侏儒类 21cm 以上，金丝熊 28cm 以上。垫料铺 15cm 以上满足打洞天性。\"},{\"h\":\"温度与食物\",\"p\":\"适宜温度 20-26℃，低于 10℃ 可能伪冬眠（危险！）。主粮为主，偶尔喂蔬菜水果小块，避开洋葱、大蒜、柑橘类。\"}]', 0, '2026-10-28 09:45:49', '2026-10-28 09:45:49');
INSERT INTO `pet_knowledge` VALUES (9, 'small', '兔子的正确饮食', '兔子不是只吃胡萝卜的，主食其实是草。', '[\"兔子\",\"饮食\",\"小宠\"]', '[{\"h\":\"牧草是主食\",\"p\":\"提摩西草等高纤维牧草要无限量供应，占饮食 80% 以上：纤维维持肠道蠕动，咀嚼磨牙防牙过长。6 月龄前可喂苜蓿草补钙。\"},{\"h\":\"兔粮与蔬菜\",\"p\":\"兔粮定量（体重的 2-3%），蔬菜逐样少量引入。胡萝卜糖分高只能当零食，「兔子吃胡萝卜」是动画里的误会。\"},{\"h\":\"危险信号\",\"p\":\"兔子 12 小时以上不吃不拉是急症（胃肠停滞），必须立刻就医，兔子耐饿能力很差，拖不得。\"}]', 0, '2026-10-28 09:45:49', '2026-10-28 09:45:49');
INSERT INTO `pet_knowledge` VALUES (10, 'bird', '鹦鹉饲养注意事项', '厨房油烟、不粘锅、风扇——家里处处是隐藏杀手。', '[\"鹦鹉\",\"鸟类\",\"安全\"]', '[{\"h\":\"呼吸系统极敏感\",\"p\":\"鸟类的呼吸系统非常娇弱：炒菜油烟、香水、杀虫喷雾、新家具的甲醛都可能致命。不粘锅空烧释放的聚四氟乙烯烟雾对鸟是剧毒，养鸟家庭烧饭要开窗通风或把鸟放远。\"},{\"h\":\"笼子与放飞\",\"p\":\"笼子宽度至少是翼展的 1.5 倍，站杆粗细要让脚爪握住后前后趾不重叠。放飞前关窗、收起吊扇、盖住镜面玻璃，防止撞伤。\"},{\"h\":\"喂食\",\"p\":\"只喂瓜子会脂肪超标，混合粮+滋养丸+少量蔬果更均衡；牛油果、巧克力、咖啡因对鸟有毒，苹果籽要去掉。\"}]', 0, '2026-10-28 09:45:49', '2026-10-28 09:45:49');
INSERT INTO `pet_knowledge` VALUES (11, 'fish', '观赏鱼换水与养水', '养鱼先养水：换水不是越勤越好，也不是越多越好。', '[\"换水\",\"水质\",\"养水\"]', '[{\"h\":\"新水要先「困」过\",\"p\":\"自来水含氯，直接加缸会伤鱼鳃和硝化细菌。接好水静置 24-48 小时，或用水质稳定剂除氯后再用。\"},{\"h\":\"换水节奏\",\"p\":\"稳定运行的缸每 1-2 周换一次，每次换 1/4 到 1/3 即可。一次全换会破坏硝化系统，水质反而崩。新水温度和缸内温差控制在 2℃ 以内。\"},{\"h\":\"过滤别洗太干净\",\"p\":\"滤材上住着硝化细菌，用原缸水轻轻漂洗就行，用自来水猛冲等于把「净水功臣」全冲掉。\"}]', 0, '2026-10-28 09:45:49', '2026-10-28 09:45:49');
INSERT INTO `pet_knowledge` VALUES (12, 'fish', '新鱼入缸四步走', '过温、过水、检疫、观察，直接倒缸是大忌。', '[\"过水\",\"开缸\",\"检疫\"]', '[{\"h\":\"过温\",\"p\":\"袋子不拆封整袋泡进缸里 15-20 分钟，让袋内水温慢慢接近缸温，避免温差应激。\"},{\"h\":\"过水\",\"p\":\"每隔几分钟往袋里舀一点缸水，让鱼适应新缸的酸碱度和硬度，累计换掉袋内一半以上的水再捞鱼入缸。袋里的水不要倒进缸。\"},{\"h\":\"检疫与观察\",\"p\":\"有条件先在检疫缸隔离观察 1-2 周；直接入缸的鱼前几天重点看是否擦缸、白点、缩鳍，早发现早处理。\"}]', 0, '2026-10-28 09:45:49', '2026-10-28 09:45:49');
INSERT INTO `pet_knowledge` VALUES (13, 'all', '宠物家庭小药箱', '体温计、碘伏、生理盐水……这些最好提前备好。', '[\"应急\",\"药箱\",\"健康\"]', '[{\"h\":\"常备物品\",\"p\":\"宠物体温计（肛温最准）、碘伏（皮肤消毒）、生理盐水（冲洗伤口眼睛）、无菌纱布、镊子、宠物专用电解质补液。人用感冒药、退烧药对猫狗可能致命，不要自行喂。\"},{\"h\":\"留好这条信息\",\"p\":\"把附近 24 小时宠物医院的位置和电话存在手机里，深夜出事时找医院最花时间。\"}]', 0, '2026-10-28 09:45:49', '2026-10-28 09:45:49');
INSERT INTO `pet_knowledge` VALUES (14, 'all', '这些情况请立刻就医', '判断不了要不要去医院时，看这一篇。', '[\"就医\",\"急救\",\"健康\"]', '[{\"h\":\"红色信号\",\"p\":\"呼吸困难、抽搐、腹部胀大（尤其狗的胃扭转）、误食有毒物质、生产超过 30 分钟无进展、尿闭（猫尤其危险）、大出血。这些是分秒必争的急症。\"},{\"h\":\"黄色信号\",\"p\":\"连续呕吐/腹泻超过 24 小时、精神萎靡躲藏、3 天不吃东西（幼猫幼犬 24 小时就要重视）、跛行持续 2 天。观察半天没好转就去医院。\"},{\"h\":\"去医院前\",\"p\":\"带上呕吐物/排泄物照片或样本、正在吃的药、既往病历，描述清楚发病时间和经过，能帮医生快很多。\"}]', 0, '2026-10-28 09:45:49', '2026-10-28 09:45:49');

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
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pet_message
-- ----------------------------
INSERT INTO `pet_message` VALUES (1, 'cutetest01', 'cutetest02', '测试02', 'cutetest01', 'cutetest01', '你好呀 cutetest02!', 1, '2026-10-23 16:42:42');
INSERT INTO `pet_message` VALUES (2, 'cutetest02', 'cutetest01', 'cutetest01', 'cutetest01', 'cutetest01', '你好呀 cutetest02!', 0, '2026-10-23 16:42:42');
INSERT INTO `pet_message` VALUES (3, 'eee', 'rrr', '44', 'eee', '33', '1', 1, '2026-10-26 10:44:42');
INSERT INTO `pet_message` VALUES (4, 'rrr', 'eee', '33', 'eee', '33', '1', 1, '2026-10-26 10:44:42');
INSERT INTO `pet_message` VALUES (5, 'eee', 'rrr', '44', 'eee', '33', '哈喽', 1, '2026-10-26 10:56:49');
INSERT INTO `pet_message` VALUES (6, 'rrr', 'eee', '33', 'eee', '33', '哈喽', 1, '2026-10-26 10:56:49');
INSERT INTO `pet_message` VALUES (7, 'rrr', 'eee', '33', 'rrr', '44', '你好', 1, '2026-10-26 10:57:18');
INSERT INTO `pet_message` VALUES (8, 'eee', 'rrr', '44', 'rrr', '44', '你好', 1, '2026-10-26 10:57:18');
INSERT INTO `pet_message` VALUES (9, 'eee', 'rrr', '44', 'eee', '33', 'hl', 1, '2026-10-28 08:53:34');
INSERT INTO `pet_message` VALUES (10, 'rrr', 'eee', '33', 'eee', '33', 'hl', 1, '2026-10-28 08:53:34');
INSERT INTO `pet_message` VALUES (11, 'rrr', 'eee', '33', 'rrr', '44', 'te', 1, '2026-10-28 08:54:09');
INSERT INTO `pet_message` VALUES (12, 'eee', 'rrr', '44', 'rrr', '44', 'te', 1, '2026-10-28 08:54:09');
INSERT INTO `pet_message` VALUES (13, 'rrr', 'eee', '33', 'rrr', '44', 'fghgfj', 1, '2026-10-28 08:54:11');
INSERT INTO `pet_message` VALUES (14, 'eee', 'rrr', '44', 'rrr', '44', 'fghgfj', 1, '2026-10-28 08:54:11');
INSERT INTO `pet_message` VALUES (15, 'rrr', 'eee', '33', 'rrr', '44', 'dgd', 1, '2026-10-28 08:54:12');
INSERT INTO `pet_message` VALUES (16, 'eee', 'rrr', '44', 'rrr', '44', 'dgd', 1, '2026-10-28 08:54:12');
INSERT INTO `pet_message` VALUES (17, 'eee', 'admin', '管理员', 'eee', '33', '1', 1, '2026-10-30 16:04:06');
INSERT INTO `pet_message` VALUES (18, 'admin', 'eee', '33', 'eee', '33', '1', 1, '2026-10-30 16:04:06');
INSERT INTO `pet_message` VALUES (19, 'eee', 'admin', '管理员', 'eee', '33', '1', 1, '2026-10-30 16:35:03');
INSERT INTO `pet_message` VALUES (20, 'admin', 'eee', '33', 'eee', '33', '1', 1, '2026-10-30 16:35:03');
INSERT INTO `pet_message` VALUES (21, 'eee', 'admin', '管理员', 'eee', '33', 'j', 1, '2026-10-30 16:35:04');
INSERT INTO `pet_message` VALUES (22, 'admin', 'eee', '33', 'eee', '33', 'j', 1, '2026-10-30 16:35:04');

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
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pet_post
-- ----------------------------
INSERT INTO `pet_post` VALUES (1, '1', '1', 'cat', '1', 'eee', '33', '2026-10-23 16:48:21');

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
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pet_post_comment
-- ----------------------------
INSERT INTO `pet_post_comment` VALUES (1, 1, 'rrr', '44', '1', '2026-10-23 16:48:52');
INSERT INTO `pet_post_comment` VALUES (2, 1, 'admin', '管理员', '1', '2026-10-30 16:37:18');
INSERT INTO `pet_post_comment` VALUES (3, 1, 'admin', '管理员', '1', '2026-10-30 16:37:18');
INSERT INTO `pet_post_comment` VALUES (4, 1, 'admin', '管理员', '1', '2026-10-30 16:37:19');
INSERT INTO `pet_post_comment` VALUES (5, 1, 'admin', '管理员', '22', '2026-10-30 16:41:17');
INSERT INTO `pet_post_comment` VALUES (6, 1, 'admin', '管理员', 'ss', '2026-10-30 16:46:16');

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
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pet_post_like
-- ----------------------------
INSERT INTO `pet_post_like` VALUES (1, 1, 'admin', '2026-10-30 16:36:45');

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
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pet_post_media
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
