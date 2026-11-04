package com.fenglin.springboottest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fenglin.springboottest.dto.CheckRequest;
import com.fenglin.springboottest.dto.LoginRequest;
import com.fenglin.springboottest.dto.ProfileUpdateRequest;
import com.fenglin.springboottest.dto.RegisterRequest;
import com.fenglin.springboottest.entity.AppUser;
import com.fenglin.springboottest.exception.AuthException;
import com.fenglin.springboottest.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 认证接口。
 * 图形验证码接口见 CaptchaController：GET /api/auth/captcha。
 * 注册已不收集邮箱；历史账号仍可用邮箱登录（login 兼容）。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** 注册：POST /api/auth/register { username, password, nickname?, captchaId, captchaCode } */
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody RegisterRequest req) {
        authService.register(req);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        Map<String, Object> user = new LinkedHashMap<>();
        user.put("username", req.getUsername());
        user.put("nickname", req.getNickname());
        m.put("user", user);
        return m;
    }

    /** 用户名占用检测：POST /api/auth/check-username { username } */
    @PostMapping("/check-username")
    public Map<String, Object> checkUsername(@RequestBody CheckRequest req) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        m.put("taken", authService.isUsernameTaken(req.getUsername()));
        return m;
    }

    /** 登录：POST /api/auth/login { account, password, remember }，account 支持用户名或历史邮箱 */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest req, HttpServletRequest request) {
        AppUser user = authService.login(req.getAccount(), req.getPassword());
        // 权限来自 app_user.role：0=管理员，1=默认（注册用户）
        Integer role = user.getRole() == null ? 1 : user.getRole();
        boolean isAdmin = role == AdminController.ROLE_ADMIN;
        // 管理员登录前台时直接授予后台会话：点「后台管理」免二次登录，直达管理面板。
        // 会话与前台共用同一个 JSESSIONID，后台接口校验的正是 AdminController.SESSION_ADMIN 属性。
        if (isAdmin) {
            HttpSession session = request.getSession(true);
            session.setAttribute(AdminController.SESSION_ADMIN, user.getUsername());
            // 管理权限永久有效：会话不因空闲而过期（约 68 年，等同永久）
            session.setMaxInactiveInterval(Integer.MAX_VALUE);
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        Map<String, Object> u = new LinkedHashMap<>();
        u.put("username", user.getUsername());
        u.put("email", user.getEmail());
        u.put("nickname", user.getNickname());
        u.put("birthday", user.getBirthday() != null ? user.getBirthday().toString() : null);
        u.put("tags", parseTags(user.getTags()));
        u.put("role", role);
        u.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
        // 是否管理员：前端据此显隐「后台管理」入口；真正的后台接口仍有会话 + 权限双重校验
        u.put("isAdmin", isAdmin);
        m.put("user", u);
        return m;
    }

    /**
     * 保存「我的资料」：POST /api/auth/profile { username, nickname?, email?, birthday?(yyyy-MM-dd), tags?[...] }
     * 持久化到 app_user.nickname / email / birthday / tags（JSON 数组字符串），退出登录后仍保留。
     */
    @PostMapping("/profile")
    public Map<String, Object> profile(@RequestBody ProfileUpdateRequest req) {
        AppUser user = authService.updateProfile(req);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        Map<String, Object> u = new LinkedHashMap<>();
        u.put("username", user.getUsername());
        u.put("nickname", user.getNickname());
        u.put("email", user.getEmail());
        u.put("birthday", user.getBirthday() != null ? user.getBirthday().toString() : null);
        u.put("tags", parseTags(user.getTags()));
        m.put("user", u);
        return m;
    }

    /** app_user.tags 存的是 JSON 数组字符串（如 ["猫奴","吃货"]），解析为 List 返回；空/坏数据返回空数组 */
    private List<String> parseTags(String tagsJson) {
        if (tagsJson == null || tagsJson.isBlank()) return new ArrayList<>();
        try {
            return new ObjectMapper().readValue(
                    tagsJson, new ObjectMapper().getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
