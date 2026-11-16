package com.fenglin.springboottest.auth;

import com.fenglin.springboottest.exception.AuthException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * 当前登录用户（基于 HttpSession）。
 *
 * 背景：此前所有内容接口（帖子/私信/好友/领养）的身份都来自前端传参（owner/userKey/author），
 * 任何人只要改一下参数就能冒充别人发帖、删帖、看私信。2026-11 起统一改为：
 *  - 登录成功时把用户名写入 Session（AuthController.login / AdminController 登录入口）；
 *  - 各接口用 require() 从 Session 取身份，前端传的同名参数仅作兼容、一律忽略。
 */
public final class CurrentUser {

    /** Session 属性名：前台登录用户名 */
    public static final String SESSION_USER = "SESSION_USER";

    private CurrentUser() {
    }

    /** 必须已登录：返回用户名；未登录抛 401（全局异常处理器会转成 JSON） */
    public static String require(HttpServletRequest request) {
        String u = get(request);
        if (u == null || u.isBlank()) {
            throw new AuthException("unauthorized", "登录状态已失效，请重新登录", 401);
        }
        return u;
    }

    /** 可选：已登录返回用户名，未登录返回 null（用于公开只读接口的个性化字段） */
    public static String get(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        HttpSession s = request.getSession(false);
        Object v = s == null ? null : s.getAttribute(SESSION_USER);
        return v == null ? null : String.valueOf(v);
    }
}
