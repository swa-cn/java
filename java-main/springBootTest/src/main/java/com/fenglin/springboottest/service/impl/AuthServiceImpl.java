package com.fenglin.springboottest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fenglin.springboottest.dto.RegisterRequest;
import com.fenglin.springboottest.entity.AppUser;
import com.fenglin.springboottest.exception.AuthException;
import com.fenglin.springboottest.mapper.AppUserMapper;
import com.fenglin.springboottest.service.AuthService;
import com.fenglin.springboottest.service.CaptchaService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Service
public class AuthServiceImpl implements AuthService {

    private final AppUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final CaptchaService captchaService;

    private static final Pattern RE_USER = Pattern.compile("^[A-Za-z0-9_]{3,20}$");
    // 密码策略（与前端 PASSWORD_POLICY 保持一致）：≥8 位、≥2 种字符
    private static final int PW_MIN_LENGTH = 8;
    private static final int PW_MIN_CLASSES = 2;

    public AuthServiceImpl(AppUserMapper userMapper, PasswordEncoder passwordEncoder, CaptchaService captchaService) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.captchaService = captchaService;
    }

    @Override
    public void register(RegisterRequest req) {
        String username = req.getUsername() == null ? "" : req.getUsername().trim();
        String password = req.getPassword() == null ? "" : req.getPassword();

        if (!RE_USER.matcher(username).matches()) {
            throw new AuthException("username", "用户名需为 3-20 位字母、数字或下划线");
        }
        if (isUsernameTaken(username)) {
            throw new AuthException("username", "该用户名已被占用", 409);
        }
        String pwMsg = validatePassword(password);
        if (pwMsg != null) {
            throw new AuthException("password", pwMsg);
        }

        // 图形验证码：一次性消费，错误即整张作废（需重新获取新图）
        if (req.getCaptchaId() == null || req.getCaptchaId().isBlank()) {
            throw new AuthException("code", "请先获取验证码");
        }
        if (req.getCaptchaCode() == null || req.getCaptchaCode().isBlank()) {
            throw new AuthException("code", "请输入图形验证码");
        }
        if (!captchaService.verify(req.getCaptchaId(), req.getCaptchaCode())) {
            throw new AuthException("code", "验证码不正确，请重新输入", 400);
        }

        AppUser user = new AppUser();
        user.setUsername(username);
        // 不再收集邮箱：email 留空（NULL），登录一律走用户名
        String nickname = req.getNickname() == null ? null : req.getNickname().trim();
        user.setNickname(nickname == null || nickname.isEmpty() ? null : nickname);
        user.setPassword(passwordEncoder.encode(password));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);
    }

    @Override
    public boolean isUsernameTaken(String username) {
        String u = username == null ? "" : username.trim().toLowerCase();
        if (u.isEmpty()) return false;
        QueryWrapper<AppUser> qw = new QueryWrapper<>();
        qw.apply("LOWER(username) = {0}", u);
        return userMapper.selectCount(qw) > 0;
    }

    @Override
    public AppUser login(String account, String password) {
        String acc = account == null ? "" : account.trim();
        QueryWrapper<AppUser> qw = new QueryWrapper<>();
        qw.apply("(username = {0} OR LOWER(email) = {1})", acc, acc.toLowerCase());
        AppUser user = userMapper.selectOne(qw);
        if (user == null) {
            throw new AuthException("notfound", "账号不存在", 401);
        }
        if (password == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new AuthException("wrongpw", "密码错误", 401);
        }
        return user;
    }

    // ---------------- 内部辅助 ----------------

    private String validatePassword(String password) {
        if (password == null) return "请输入密码";
        if (password.length() < PW_MIN_LENGTH) {
            return "密码至少 " + PW_MIN_LENGTH + " 位";
        }
        if (password.length() > 128) {
            return "密码过长（最多 128 位）";
        }
        int classes = 0;
        if (password.matches(".*[a-z].*")) classes++;
        if (password.matches(".*[A-Z].*")) classes++;
        if (password.matches(".*[0-9].*")) classes++;
        if (password.matches(".*[^A-Za-z0-9].*")) classes++;
        if (classes < PW_MIN_CLASSES) {
            return "密码至少包含 " + PW_MIN_CLASSES + " 种字符（小写 / 大写 / 数字 / 符号）";
        }
        return null;
    }
}
