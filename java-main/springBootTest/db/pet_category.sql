-- =====================================================================
-- 萌宠分类 / 品种 建表 + 种子数据（幂等，可重复执行）
-- 与前端 login-vue/src/composables/petData.js 的数据一一对应
-- =====================================================================

CREATE TABLE IF NOT EXISTS aurora.pet_category (
  id      VARCHAR(20)  NOT NULL COMMENT '分类 id：cat/dog/small/bird/fish',
  name    VARCHAR(40)  NOT NULL COMMENT '分类名',
  icon    VARCHAR(10)           COMMENT '单字图标',
  color   VARCHAR(20)           COMMENT '主题色',
  image   VARCHAR(300)          COMMENT '封面图',
  sort_no INT          NOT NULL DEFAULT 0 COMMENT '排序（升序）',
  PRIMARY KEY (id),
  KEY idx_sort (sort_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='萌宠分类';

CREATE TABLE IF NOT EXISTS aurora.pet_breed (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  category_id VARCHAR(20)  NOT NULL COMMENT '所属分类 id',
  name        VARCHAR(40)  NOT NULL COMMENT '品种名',
  image       VARCHAR(300)          COMMENT '品种图',
  description VARCHAR(200)          COMMENT '品种简介',
  sort_no     INT          NOT NULL DEFAULT 0 COMMENT '排序（升序）',
  PRIMARY KEY (id),
  KEY idx_category_sort (category_id, sort_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='萌宠品种图鉴';

-- ---------------- 分类种子数据（幂等） ----------------
INSERT INTO aurora.pet_category (id, name, icon, color, image, sort_no) VALUES
  ('cat',   '猫类', '喵', '#ff6fa8', '/images/pets/cat.png',     1),
  ('dog',   '狗类', '汪', '#ff8a5c', '/images/pets/dog.png',     2),
  ('small', '小宠', '啮', '#8b7bff', '/images/pets/hamster.png', 3),
  ('bird',  '鸟类', '啾', '#3ec4a8', '/images/pets/bird.png',    4),
  ('fish',  '水族', '鱼', '#4ab3e8', '/images/pets/fish.png',    5)
ON DUPLICATE KEY UPDATE
  name = VALUES(name), icon = VALUES(icon), color = VALUES(color),
  image = VALUES(image), sort_no = VALUES(sort_no);

-- ---------------- 品种种子数据（先清后插，保证与前端一致） ----------------
DELETE FROM aurora.pet_breed;

INSERT INTO aurora.pet_breed (category_id, name, image, description, sort_no) VALUES
  ('cat', '布偶猫',     '/images/pets/cat.png',          '温顺黏人，蓝眼睛像小天使', 1),
  ('cat', '英国短毛猫', '/images/pets/cat_british.png',  '圆脸胖墩，慵懒小可爱',     2),
  ('cat', '缅因猫',     '/images/pets/cat_maine.png',    '体型巨大，温柔的毛毛怪',   3),
  ('cat', '橘猫',       '/images/pets/cat_orange.png',   '十橘九胖，干饭第一名',     4),
  ('cat', '暹罗猫',     '/images/pets/cat_siamese.png',  '话痨小煤球，超爱撒娇',     5),
  ('dog', '柯基',       '/images/pets/dog.png',          '蜜桃臀小短腿，快乐源泉',   1),
  ('dog', '金毛',       '/images/pets/dog_golden.png',   '暖男代表，温柔大暖炉',     2),
  ('dog', '柴犬',       '/images/pets/dog_shiba.png',    '表情包担当，倔强又可爱',   3),
  ('dog', '哈士奇',     '/images/pets/dog_husky.png',    '行走的拆迁队，二哈本哈',   4),
  ('dog', '边牧',       '/images/pets/dog_border.png',   '狗界智商天花板，运动健将', 5),
  ('small', '仓鼠',     '/images/pets/hamster.png',         '腮帮子仓库，囤粮小能手', 1),
  ('small', '龙猫',     '/images/pets/small_chinchilla.png','软糯棉花糖，怕热小公主', 2),
  ('small', '兔子',     '/images/pets/small_rabbit.png',    '长耳吃货，安静又软萌',   3),
  ('small', '荷兰猪',   '/images/pets/small_guinea.png',    '咕咕叫的小胖墩，超亲人', 4),
  ('bird', '鹦鹉',      '/images/pets/bird.png',            '模仿达人，话痨小歌手',   1),
  ('bird', '文鸟',      '/images/pets/bird_bunting.png',    '圆滚滚小白团，安静温顺', 2),
  ('bird', '玄凤',      '/images/pets/bird_cockatiel.png',  '黄冠腮红，吹口哨高手',   3),
  ('bird', '鸽子',      '/images/pets/bird_pigeon.png',     '和平使者，咕咕小邮差',   4),
  ('fish', '金鱼',      '/images/pets/fish.png',            '圆肚小福星，慢悠悠摆尾', 1),
  ('fish', '锦鲤',      '/images/pets/fish_koi.png',        '好运象征，池中花团锦簇', 2),
  ('fish', '斗鱼',      '/images/pets/fish_betta.png',      '飘逸长鳍，孤傲小武士',   3),
  ('fish', '热带鱼',    '/images/pets/fish_tropical.png',   '色彩斑斓，群游小精灵',   4);
