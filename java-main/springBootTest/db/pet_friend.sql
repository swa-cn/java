-- =====================================================================
-- 好友 + 私信建表脚本（可重复执行，幂等）
--
-- 说明：后端启动时会由 SchemaInitializer 自动建这两张表，
-- 本文件用于手动初始化 / 迁移到其它环境时执行。
--
-- 导入命令（Windows 命令行）：
--   mysql -uroot -p123456 --default-character-set=utf8mb4 < pet_friend.sql
-- =====================================================================

CREATE DATABASE IF NOT EXISTS aurora CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE aurora;

-- ---------------- 好友关系 ----------------
-- 成为好友写双向两条（A→B、B→A）；申请阶段只写一条（status=pending）
CREATE TABLE IF NOT EXISTS pet_friend (
  id BIGINT NOT NULL AUTO_INCREMENT,
  owner_key VARCHAR(20) NOT NULL COMMENT '关系归属方登录名',
  friend_key VARCHAR(20) NOT NULL COMMENT '对方登录名',
  friend_name VARCHAR(24) COMMENT '对方昵称（展示用）',
  status VARCHAR(16) NOT NULL DEFAULT 'pending' COMMENT 'friend / pending',
  source VARCHAR(16) DEFAULT 'manual' COMMENT 'post / search / profile / manual',
  created_at DATETIME,
  accepted_at DATETIME,
  PRIMARY KEY (id),
  UNIQUE KEY uk_owner_friend (owner_key, friend_key),
  KEY idx_owner_status (owner_key, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='好友关系';

-- ---------------- 好友私信 ----------------
-- 一次发送写两条：自己一侧 read_flag=1，对方一侧 read_flag=0
CREATE TABLE IF NOT EXISTS pet_message (
  id BIGINT NOT NULL AUTO_INCREMENT,
  owner_key VARCHAR(20) NOT NULL COMMENT '会话归属方登录名',
  peer_key VARCHAR(20) NOT NULL COMMENT '会话对方登录名',
  peer_name VARCHAR(24) COMMENT '对方昵称',
  from_key VARCHAR(20) NOT NULL COMMENT '实际发送者登录名',
  from_name VARCHAR(24) COMMENT '发送者昵称',
  content VARCHAR(500) NOT NULL COMMENT '消息正文',
  read_flag TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否已读',
  created_at DATETIME,
  PRIMARY KEY (id),
  KEY idx_owner_peer (owner_key, peer_key),
  KEY idx_owner_created (owner_key, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='好友私信';

SELECT 'pet_friend / pet_message 已就绪' AS result;
