package com.fenglin.springboottest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fenglin.springboottest.dto.ProfileUpdateRequest;
import com.fenglin.springboottest.dto.RegisterRequest;
import com.fenglin.springboottest.entity.AppUser;
import com.fenglin.springboottest.exception.AuthException;
import com.fenglin.springboottest.mapper.AppUserMapper;
import com.fenglin.springboottest.service.AuthService;
import com.fenglin.springboottest.service.CaptchaService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 认证实现。
 *
 * ⚠️ 密码策略变更（需求指定）：密码以「明文」保存与比对，不再做 BCrypt 哈希。
 *    这是内部学习/演示项目的要求，真实环境必须加盐哈希（bcrypt/argon2），不可明文。
 *
 * 权限：app_user.role —— 0=管理员，1=默认（注册用户）。
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final AppUserMapper userMapper;
    private final CaptchaService captchaService;

    private static final Pattern RE_USER = Pattern.compile("^[A-Za-z0-9_]{3,20}$");
    // 密码策略（与前端 PASSWORD_POLICY 保持一致）：≥8 位、≥2 种字符
    private static final int PW_MIN_LENGTH = 8;
    private static final int PW_MIN_CLASSES = 2;

    /** 权限常量：0=管理员，1=默认 */
    public static final int ROLE_ADMIN = 0;
    public static final int ROLE_DEFAULT = 1;

    public AuthServiceImpl(AppUserMapper userMapper, CaptchaService captchaService) {
        this.userMapper = userMapper;
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
        // 明文密码（需求指定）：不做加密，原样入库
        user.setPassword(password);
        // 注册用户统一为「默认权限」；管理员权限只能由管理员/DBA 在库中设置 role=0
        user.setRole(ROLE_DEFAULT);
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
        // 明文比对（需求指定）
        String stored = user.getPassword();
        if (password == null || stored == null || !stored.equals(password)) {
            throw new AuthException("wrongpw", "密码错误", 401);
        }
        return user;
    }

    // ---------------- 我的资料（设置面板） ----------------

    private static final int TAGS_MAX_COUNT = 6;
    private static final int TAG_MAX_LENGTH = 8;
    private static final int NICKNAME_MAX_LENGTH = 20;
    private static final int EMAIL_MAX_LENGTH = 120;
    private static final Pattern RE_EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @Override
    public AppUser updateProfile(ProfileUpdateRequest req) {
        String username = req.getUsername() == null ? "" : req.getUsername().trim();
        if (username.isEmpty()) {
            throw new AuthException("username", "缺少用户名，请重新登录", 401);
        }
        QueryWrapper<AppUser> qw = new QueryWrapper<>();
        qw.apply("LOWER(username) = {0}", username.toLowerCase());
        AppUser user = userMapper.selectOne(qw);
        if (user == null) {
            throw new AuthException("notfound", "账号不存在", 401);
        }

        // 昵称：可清空；超长兜底报错
        String nickname = req.getNickname() == null ? null : req.getNickname().trim();
        if (nickname != null && nickname.length() > NICKNAME_MAX_LENGTH) {
            throw new AuthException("nickname", "昵称最长 " + NICKNAME_MAX_LENGTH + " 字", 400);
        }
        user.setNickname(nickname == null || nickname.isEmpty() ? null : nickname);

        // 邮箱：可清空；填写时校验格式与唯一性（uk_email 唯一索引）
        String email = req.getEmail() == null ? "" : req.getEmail().trim();
        if (email.isEmpty()) {
            user.setEmail(null);
        } else {
            if (email.length() > EMAIL_MAX_LENGTH || !RE_EMAIL.matcher(email).matches()) {
                throw new AuthException("email", "邮箱格式不正确", 400);
            }
            QueryWrapper<AppUser> eq = new QueryWrapper<>();
            eq.apply("LOWER(email) = {0}", email.toLowerCase());
            eq.ne("id", user.getId());
            if (userMapper.selectCount(eq) > 0) {
                throw new AuthException("email", "该邮箱已被其他账号使用", 409);
            }
            user.setEmail(email);
        }

        // 生日：yyyy-MM-dd，空串视为清除
        String birthday = req.getBirthday() == null ? "" : req.getBirthday().trim();
        if (birthday.isEmpty()) {
            user.setBirthday(null);
        } else {
            try {
                user.setBirthday(LocalDate.parse(birthday));
            } catch (DateTimeParseException e) {
                throw new AuthException("birthday", "生日格式应为 yyyy-MM-dd", 400);
            }
        }

        // 个性标签：JSON 数组字符串入库；空列表视为清除
        List<String> tags = req.getTags();
        if (tags == null || tags.isEmpty()) {
            user.setTags(null);
        } else {
            List<String> clean = new ArrayList<>();
            for (String t : tags) {
                String v = t == null ? "" : t.trim();
                if (v.isEmpty() || clean.contains(v)) continue;
                if (v.length() > TAG_MAX_LENGTH) {
                    throw new AuthException("tags", "每个标签最长 " + TAG_MAX_LENGTH + " 字", 400);
                }
                clean.add(v);
                if (clean.size() >= TAGS_MAX_COUNT) break;
            }
            if (clean.isEmpty()) {
                user.setTags(null);
            } else {
                try {
                    user.setTags(new ObjectMapper().writeValueAsString(clean));
                } catch (Exception e) {
                    throw new AuthException("tags", "标签序列化失败", 500);
                }
            }
        }

        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
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
