package com.fenglin.springboottest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fenglin.springboottest.entity.AppUser;
import com.fenglin.springboottest.exception.AuthException;
import com.fenglin.springboottest.mapper.AppUserMapper;
import com.fenglin.springboottest.service.AdminUserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 后台「用户管理」实现（app_user 表）。
 *
 * 密码策略与 AuthServiceImpl 保持一致（≥8 位、≥2 种字符），明文入库（项目既定策略）。
 */
@Service
public class AdminUserServiceImpl implements AdminUserService {

    private final AppUserMapper userMapper;

    /** 超级管理员账号（最大权限，只有它能任免其他管理员） */
    @Value("${admin.username:admin}")
    private String superUsername;

    private static final Pattern RE_USER = Pattern.compile("^[A-Za-z0-9_]{3,20}$");
    private static final int PW_MIN_LENGTH = 8;
    private static final int PW_MIN_CLASSES = 2;

    /** 权限常量：app_user.role —— 0=管理员，1=默认 */
    public static final int ROLE_ADMIN = 0;
    public static final int ROLE_DEFAULT = 1;

    public AdminUserServiceImpl(AppUserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public List<Map<String, Object>> listUsers(String keyword) {
        QueryWrapper<AppUser> qw = new QueryWrapper<>();
        String kw = keyword == null ? "" : keyword.trim();
        if (!kw.isEmpty()) {
            qw.and(w -> w.like("username", kw).or().like("nickname", kw));
        }
        qw.orderByAsc("id");
        List<AppUser> users = userMapper.selectList(qw);
        List<Map<String, Object>> out = new ArrayList<>(users.size());
        for (AppUser u : users) out.add(view(u));
        return out;
    }

    @Override
    public Map<String, Object> create(Map<String, Object> body, boolean allowAdminRole) {
        String username = str(body, "username");
        String password = str(body, "password");
        String nickname = str(body, "nickname");
        Integer role = intVal(body.get("role"), ROLE_DEFAULT);

        if (username == null || !RE_USER.matcher(username).matches()) {
            throw new AuthException("username", "用户名需为 3-20 位字母、数字或下划线");
        }
        if (isUsernameTaken(username)) {
            throw new AuthException("username", "该用户名已被占用", 409);
        }
        String pwMsg = validatePassword(password);
        if (pwMsg != null) throw new AuthException("password", pwMsg);
        if (role != ROLE_ADMIN && role != ROLE_DEFAULT) {
            throw new AuthException("role", "权限值不合法");
        }
        if (role == ROLE_ADMIN && !allowAdminRole) {
            throw new AuthException("forbidden", "只有超级管理员（admin 账号）才能添加管理员", 403);
        }

        AppUser user = new AppUser();
        user.setUsername(username);
        user.setNickname(nickname == null || nickname.isEmpty() ? null : nickname);
        user.setPassword(password);
        user.setRole(role);
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        userMapper.insert(user);
        return view(user);
    }

    @Override
    public Map<String, Object> update(Long id, Map<String, Object> patch, boolean allowAdminRole) {
        AppUser user = userMapper.selectById(id);
        if (user == null) throw new AuthException("notfound", "用户不存在", 404);

        String nickname = str(patch, "nickname");
        if (nickname != null) {
            user.setNickname(nickname.isEmpty() ? null : nickname);
        }

        String password = str(patch, "password");
        if (password != null && !password.isEmpty()) {
            String pwMsg = validatePassword(password);
            if (pwMsg != null) throw new AuthException("password", pwMsg);
            user.setPassword(password);
        }

        if (patch.containsKey("role")) {
            Integer role = intVal(patch.get("role"), user.getRole());
            if (role != ROLE_ADMIN && role != ROLE_DEFAULT) {
                throw new AuthException("role", "权限值不合法");
            }
            if (!role.equals(user.getRole())) {
                if (isSuperAccount(user.getUsername())) {
                    throw new AuthException("forbidden", "超级管理员账号（admin）的权限不可修改", 403);
                }
                if (!allowAdminRole) {
                    throw new AuthException("forbidden", "只有超级管理员（admin 账号）才能设置管理员权限", 403);
                }
            }
            user.setRole(role);
        }

        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        return view(user);
    }

    @Override
    public void delete(Long id, String currentUsername, boolean allowAdminRole) {
        AppUser user = userMapper.selectById(id);
        if (user == null) throw new AuthException("notfound", "用户不存在", 404);
        if (isSuperAccount(user.getUsername())) {
            throw new AuthException("forbidden", "超级管理员账号（admin）不可删除", 403);
        }
        if (user.getUsername().equalsIgnoreCase(currentUsername)) {
            throw new AuthException("self", "不能删除当前登录的账号");
        }
        if (user.getRole() != null && user.getRole() == ROLE_ADMIN && !allowAdminRole) {
            throw new AuthException("forbidden", "只有超级管理员（admin 账号）才能删除管理员", 403);
        }
        userMapper.deleteById(id);
    }

    /* ---------------- 内部辅助 ---------------- */

    private boolean isSuperAccount(String username) {
        return superUsername.equalsIgnoreCase(username);
    }

    private boolean isUsernameTaken(String username) {
        QueryWrapper<AppUser> qw = new QueryWrapper<>();
        qw.apply("LOWER(username) = {0}", username.trim().toLowerCase());
        return userMapper.selectCount(qw) > 0;
    }

    /** 输出结构（不含密码）：{ id, username, nickname, email, role, admin, superAdmin, createdAt } */
    private Map<String, Object> view(AppUser u) {
        Map<String, Object> v = new java.util.LinkedHashMap<>();
        v.put("id", u.getId());
        v.put("username", u.getUsername());
        v.put("nickname", u.getNickname());
        v.put("email", u.getEmail());
        int role = u.getRole() == null ? ROLE_DEFAULT : u.getRole();
        v.put("role", role);
        v.put("admin", role == ROLE_ADMIN);
        v.put("superAdmin", isSuperAccount(u.getUsername()));
        v.put("createdAt", u.getCreatedAt());
        return v;
    }

    private static String str(Map<String, Object> m, String key) {
        Object v = m == null ? null : m.get(key);
        if (v == null) return null;
        String s = String.valueOf(v).trim();
        return s.isEmpty() ? null : s;
    }

    private static Integer intVal(Object v, Integer fallback) {
        if (v == null) return fallback;
        if (v instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(String.valueOf(v).trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    /** 与 AuthServiceImpl 相同的密码策略：≥8 位、≥2 种字符 */
    private String validatePassword(String password) {
        if (password == null || password.isEmpty()) return "请输入密码";
        if (password.length() < PW_MIN_LENGTH) return "密码至少 " + PW_MIN_LENGTH + " 位";
        if (password.length() > 128) return "密码过长（最多 128 位）";
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
