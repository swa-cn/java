package com.fenglin.springboottest.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * 启动即自建库与表（无需手动执行 SQL / 无需 mysql 客户端）。
 * - 连接到「无库」地址，执行 CREATE DATABASE IF NOT EXISTS aurora
 * - 在 aurora 下 CREATE TABLE IF NOT EXISTS app_user
 *
 * 注册自 2026-10 起不再收集邮箱：email 列允许 NULL（历史账号仍带值）。
 * 图形验证码存内存（CaptchaService），无需建表；旧的 email_code 表不再创建
 * （对已存在的旧库，可在 MySQL 里手动 DROP TABLE IF EXISTS aurora.email_code）。
 *
 * 注意：JDBC 的 Statement.execute() 默认只执行单条 SQL，不能把多条以分号拼接的
 * DDL 一次性发给 MySQL（会报语法错误）。因此这里逐条 execute，每条独立、幂等安全。
 */
@Component
public class SchemaInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SchemaInitializer.class);

    @Value("${auth.db.admin-url}")
    private String adminUrl;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;

    private static final String CREATE_DB =
            "CREATE DATABASE IF NOT EXISTS aurora CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci";

    private static final String CREATE_APP_USER = """
            CREATE TABLE IF NOT EXISTS aurora.app_user (
              id BIGINT NOT NULL AUTO_INCREMENT,
              username VARCHAR(20) NOT NULL,
              email VARCHAR(120) NULL,
              nickname VARCHAR(24),
              password VARCHAR(100) NOT NULL,
              created_at DATETIME,
              updated_at DATETIME,
              PRIMARY KEY (id),
              UNIQUE KEY uk_username (username),
              UNIQUE KEY uk_email (email)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """;

    /** 兼容旧库：email 原为 NOT NULL，改为可空（幂等，每次启动执行也不影响数据） */
    private static final String ALTER_EMAIL_NULL =
            "ALTER TABLE aurora.app_user MODIFY email VARCHAR(120) NULL";

    /* ---------------- 「发现」帖子模块（pet_post 系列） ---------------- */

    private static final String CREATE_PET_POST = """
            CREATE TABLE IF NOT EXISTS aurora.pet_post (
              id BIGINT NOT NULL AUTO_INCREMENT,
              title VARCHAR(80),
              content TEXT,
              category VARCHAR(20) NOT NULL DEFAULT 'cat',
              tags VARCHAR(200),
              author VARCHAR(20) NOT NULL,
              author_name VARCHAR(24),
              created_at DATETIME,
              PRIMARY KEY (id),
              KEY idx_category (category),
              KEY idx_created (created_at)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """;

    private static final String CREATE_PET_POST_MEDIA = """
            CREATE TABLE IF NOT EXISTS aurora.pet_post_media (
              id BIGINT NOT NULL AUTO_INCREMENT,
              post_id BIGINT NOT NULL,
              media_type VARCHAR(10) NOT NULL,
              url VARCHAR(300) NOT NULL,
              name VARCHAR(200),
              size_bytes BIGINT,
              sort_no INT,
              PRIMARY KEY (id),
              KEY idx_post (post_id)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """;

    private static final String CREATE_PET_POST_LIKE = """
            CREATE TABLE IF NOT EXISTS aurora.pet_post_like (
              id BIGINT NOT NULL AUTO_INCREMENT,
              post_id BIGINT NOT NULL,
              user_key VARCHAR(20) NOT NULL,
              created_at DATETIME,
              PRIMARY KEY (id),
              UNIQUE KEY uk_post_user (post_id, user_key)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """;

    private static final String CREATE_PET_POST_COMMENT = """
            CREATE TABLE IF NOT EXISTS aurora.pet_post_comment (
              id BIGINT NOT NULL AUTO_INCREMENT,
              post_id BIGINT NOT NULL,
              author VARCHAR(20) NOT NULL,
              author_name VARCHAR(24),
              content VARCHAR(300) NOT NULL,
              created_at DATETIME,
              PRIMARY KEY (id),
              KEY idx_post (post_id)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """;

    /* ---------------- 好友关系 / 私信（pet_friend / pet_message） ---------------- */

    /**
     * 好友关系表。
     * 成为好友时写双向两条记录（A→B、B→A），各自的好友列表只需按 owner_key 查一次；
     * 申请阶段只写一条（申请人 → 对方，status=pending）。
     */
    private static final String CREATE_PET_FRIEND = """
            CREATE TABLE IF NOT EXISTS aurora.pet_friend (
              id BIGINT NOT NULL AUTO_INCREMENT,
              owner_key VARCHAR(20) NOT NULL,
              friend_key VARCHAR(20) NOT NULL,
              friend_name VARCHAR(24),
              status VARCHAR(16) NOT NULL DEFAULT 'pending',
              source VARCHAR(16) DEFAULT 'manual',
              created_at DATETIME,
              accepted_at DATETIME,
              PRIMARY KEY (id),
              UNIQUE KEY uk_owner_friend (owner_key, friend_key),
              KEY idx_owner_status (owner_key, status)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """;

    /**
     * 好友私信表。每条是一句话，按 (owner_key, peer_key) 聚合为会话。
     * 发送时写两条：自己一侧（read=1）+ 对方一侧（read=0，用于未读统计）。
     */
    private static final String CREATE_PET_MESSAGE = """
            CREATE TABLE IF NOT EXISTS aurora.pet_message (
              id BIGINT NOT NULL AUTO_INCREMENT,
              owner_key VARCHAR(20) NOT NULL,
              peer_key VARCHAR(20) NOT NULL,
              peer_name VARCHAR(24),
              from_key VARCHAR(20) NOT NULL,
              from_name VARCHAR(24),
              content VARCHAR(500) NOT NULL,
              read_flag TINYINT(1) NOT NULL DEFAULT 1,
              created_at DATETIME,
              PRIMARY KEY (id),
              KEY idx_owner_peer (owner_key, peer_key),
              KEY idx_owner_created (owner_key, created_at)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """;

    /* ---------------- 萌宠分类 / 品种图鉴（pet_category 系列） ---------------- */

    private static final String CREATE_PET_CATEGORY = """
            CREATE TABLE IF NOT EXISTS aurora.pet_category (
              id VARCHAR(20) NOT NULL,
              name VARCHAR(40) NOT NULL,
              icon VARCHAR(10),
              color VARCHAR(20),
              image VARCHAR(300),
              sort_no INT NOT NULL DEFAULT 0,
              PRIMARY KEY (id),
              KEY idx_sort (sort_no)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """;

    private static final String CREATE_PET_BREED = """
            CREATE TABLE IF NOT EXISTS aurora.pet_breed (
              id BIGINT NOT NULL AUTO_INCREMENT,
              category_id VARCHAR(20) NOT NULL,
              name VARCHAR(40) NOT NULL,
              image VARCHAR(300),
              description VARCHAR(200),
              sort_no INT NOT NULL DEFAULT 0,
              PRIMARY KEY (id),
              KEY idx_category_sort (category_id, sort_no)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """;

    /**
     * 分类种子数据：幂等写入（已存在则更新名称/图标/颜色/排序，不覆盖用户后续改动之外的结构）。
     * 与前端 petData.js 的 5 大类保持一致。
     */
    private static final String SEED_PET_CATEGORY = """
            INSERT INTO aurora.pet_category (id, name, icon, color, image, sort_no) VALUES
              ('cat',   '猫类', '喵', '#ff6fa8', '/images/pets/cat.png',     1),
              ('dog',   '狗类', '汪', '#ff8a5c', '/images/pets/dog.png',     2),
              ('small', '小宠', '啮', '#8b7bff', '/images/pets/hamster.png', 3),
              ('bird',  '鸟类', '啾', '#3ec4a8', '/images/pets/bird.png',    4),
              ('fish',  '水族', '鱼', '#4ab3e8', '/images/pets/fish.png',    5)
            ON DUPLICATE KEY UPDATE
              name = VALUES(name), icon = VALUES(icon), color = VALUES(color),
              image = VALUES(image), sort_no = VALUES(sort_no)
            """;

    /**
     * 品种种子数据：仅当表为空时写入，避免每次启动清空表（用户可能已自行增删品种）。
     * 需要重置为初始 22 个品种时，执行 db/pet_category.sql 即可。
     */
    private static final String COUNT_PET_BREED = "SELECT COUNT(*) FROM aurora.pet_breed";

    private static final String SEED_PET_BREED = """
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
              ('fish', '热带鱼',    '/images/pets/fish_tropical.png',   '色彩斑斓，群游小精灵',   4)
            """;

    @Override
    public void run(String... args) {
        try (Connection conn = DriverManager.getConnection(adminUrl, username, password);
             Statement st = conn.createStatement()) {
            st.execute(CREATE_DB);
            log.info("数据库 aurora 已就绪（若不存在则已创建）");
            st.execute(CREATE_APP_USER);
            st.execute(ALTER_EMAIL_NULL);
            log.info("数据表 aurora.app_user 已就绪（email 允许为空，注册不再收集邮箱）");
            st.execute(CREATE_PET_POST);
            st.execute(CREATE_PET_POST_MEDIA);
            st.execute(CREATE_PET_POST_LIKE);
            st.execute(CREATE_PET_POST_COMMENT);
            log.info("数据表 aurora.pet_post / pet_post_media / pet_post_like / pet_post_comment 已就绪");
            st.execute(CREATE_PET_CATEGORY);
            st.execute(CREATE_PET_BREED);
            st.execute(SEED_PET_CATEGORY);
            seedBreedsIfEmpty(st);
            log.info("数据表 aurora.pet_category / pet_breed 已就绪（分类 5 类，品种初始数据已写入）");
            st.execute(CREATE_PET_FRIEND);
            st.execute(CREATE_PET_MESSAGE);
            log.info("数据表 aurora.pet_friend / pet_message 已就绪（好友关系 + 私信）");
        } catch (Exception e) {
            log.error("建库建表失败：{}", e.getMessage(), e);
            throw new IllegalStateException("Schema 初始化失败，请检查 MySQL 是否启动 / 账号密码是否正确", e);
        }
    }

    /** 品种表为空时才灌入初始数据，避免每次启动重置用户自行维护的品种 */
    private void seedBreedsIfEmpty(Statement st) throws Exception {
        try (var rs = st.executeQuery(COUNT_PET_BREED)) {
            if (rs.next() && rs.getInt(1) > 0) {
                log.info("pet_breed 已有数据，跳过初始品种写入");
                return;
            }
        }
        st.execute(SEED_PET_BREED);
    }
}
