package com.fenglin.springboottest.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码加密工具（BCrypt）。
 *
 * 背景：2026-11 之前密码以明文入库（历史策略）。改为 BCrypt 后需要兼容存量账号：
 *  - 新密码一律 encode() 后入库（哈希串自带随机盐，形如 $2a$10$...，长度 60）；
 *  - 校验用 matches()：库里是哈希就走 BCrypt 比对，库里还是旧明文就按明文比对，
 *    并由调用方在登录成功后把明文升级为哈希（对老用户无感知）。
 */
public final class PasswordUtil {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private PasswordUtil() {
    }

    /** 判断库里存的密码是否已是 BCrypt 哈希（$2 开头、长度 60） */
    public static boolean isHashed(String stored) {
        return stored != null && stored.length() == 60 && stored.startsWith("$2");
    }

    /** 加密（BCrypt，每次结果不同，因为盐随机） */
    public static String encode(String raw) {
        return ENCODER.encode(raw);
    }

    /** 校验：兼容 BCrypt 哈希与旧明文两种存储格式 */
    public static boolean matches(String raw, String stored) {
        if (raw == null || stored == null) {
            return false;
        }
        if (isHashed(stored)) {
            return ENCODER.matches(raw, stored);
        }
        return stored.equals(raw);
    }
}
