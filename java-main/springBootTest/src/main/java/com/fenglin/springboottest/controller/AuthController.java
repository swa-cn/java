package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.dto.CheckRequest;
import com.fenglin.springboottest.dto.LoginRequest;
import com.fenglin.springboottest.dto.RegisterRequest;
import com.fenglin.springboottest.entity.AppUser;
import com.fenglin.springboottest.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
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
    public Map<String, Object> login(@RequestBody LoginRequest req) {
        AppUser user = authService.login(req.getAccount(), req.getPassword());
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        Map<String, Object> u = new LinkedHashMap<>();
        u.put("username", user.getUsername());
        u.put("email", user.getEmail());
        u.put("nickname", user.getNickname());
        u.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
        m.put("user", u);
        return m;
    }
}
