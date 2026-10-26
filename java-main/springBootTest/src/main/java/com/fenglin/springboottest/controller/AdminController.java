package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.dto.AdminLoginRequest;
import com.fenglin.springboottest.entity.AppUser;
import com.fenglin.springboottest.entity.PetBreed;
import com.fenglin.springboottest.entity.PetCategory;
import com.fenglin.springboottest.exception.AuthException;
import com.fenglin.springboottest.service.AuthService;
import com.fenglin.springboottest.service.PetAdminService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 后台管理系统接口（/api/admin/*）。
 *
 * 登录方式：账号 + 密码（复用 /api/auth/login 的 BCrypt 校验），
 * 且账号必须等于配置的管理员账号 admin.username（默认 admin）。
 * 登录成功后写 HttpSession（属性名 admin.user），后续接口统一校验会话。
 *
 * 能力：对首页「萌宠分类」的数据（pet_category / pet_breed 两张表）做增删改：
 *  POST   /api/admin/login               管理员登录
 *  GET    /api/admin/me                  当前登录态
 *  POST   /api/admin/logout              退出后台
 *  GET    /api/admin/categories          分类列表（含品种数）
 *  POST   /api/admin/categories          新增分类
 *  PUT    /api/admin/categories/{id}     修改分类属性
 *  DELETE /api/admin/categories/{id}     删除分类（连带品种）
 *  GET    /api/admin/breeds?categoryId=  分类下的品种列表
 *  POST   /api/admin/breeds              新增品种
 *  PUT    /api/admin/breeds/{id}         修改品种
 *  DELETE /api/admin/breeds/{id}         删除品种
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    /** 会话里标记管理员的属性名 */
    public static final String SESSION_ADMIN = "admin.user";

    /** 权限常量：app_user.role —— 0=管理员，1=默认 */
    public static final int ROLE_ADMIN = 0;

    private final AuthService authService;
    private final PetAdminService petAdminService;

    public AdminController(AuthService authService, PetAdminService petAdminService) {
        this.authService = authService;
        this.petAdminService = petAdminService;
    }

    /* ================= 登录 / 登出 ================= */

    /** 管理员登录：只看 app_user.role，role=0 才允许登录后台，否则 403 */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody AdminLoginRequest req, HttpServletRequest request) {
        AppUser user = authService.login(req.getAccount(), req.getPassword());
        int role = user.getRole() == null ? 1 : user.getRole();
        if (role != ROLE_ADMIN) {
            throw new AuthException("forbidden", "该账号不是管理员账号", 403);
        }
        // 防会话固定：登录成功后重建会话再写入管理员标记
        HttpSession old = request.getSession(false);
        if (old != null) {
            old.invalidate();
        }
        HttpSession session = request.getSession(true);
        session.setAttribute(SESSION_ADMIN, user.getUsername());
        // 管理权限永久有效：会话不因空闲而过期（约 68 年）
        session.setMaxInactiveInterval(Integer.MAX_VALUE);

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        Map<String, Object> u = new LinkedHashMap<>();
        u.put("username", user.getUsername());
        u.put("nickname", user.getNickname() != null ? user.getNickname() : "管理员");
        m.put("user", u);
        return m;
    }

    /** 当前登录态：已登录返回管理员信息，未登录 401（前端据此跳回登录页） */
    @GetMapping("/me")
    public Map<String, Object> me(HttpSession session) {
        String admin = requireAdmin(session);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        m.put("user", Map.of("username", admin));
        return m;
    }

    /** 退出后台：销毁会话 */
    @PostMapping("/logout")
    public Map<String, Object> logout(HttpSession session) {
        session.invalidate();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        return m;
    }

    /* ================= 分类管理 ================= */

    @GetMapping("/categories")
    public Map<String, Object> listCategories(HttpSession session) {
        requireAdmin(session);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        m.put("items", petAdminService.listCategories());
        return m;
    }

    @PostMapping("/categories")
    public Map<String, Object> createCategory(@RequestBody PetCategory body, HttpSession session) {
        requireAdmin(session);
        PetCategory c = petAdminService.createCategory(body);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        m.put("category", c);
        return m;
    }

    @PutMapping("/categories/{id}")
    public Map<String, Object> updateCategory(@PathVariable String id, @RequestBody PetCategory patch,
                                              HttpSession session) {
        requireAdmin(session);
        PetCategory c = petAdminService.updateCategory(id, patch);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        m.put("category", c);
        return m;
    }

    @DeleteMapping("/categories/{id}")
    public Map<String, Object> deleteCategory(@PathVariable String id, HttpSession session) {
        requireAdmin(session);
        petAdminService.deleteCategory(id);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        return m;
    }

    /* ================= 品种管理 ================= */

    @GetMapping("/breeds")
    public Map<String, Object> listBreeds(@RequestParam String categoryId, HttpSession session) {
        requireAdmin(session);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        m.put("items", petAdminService.listBreeds(categoryId));
        return m;
    }

    @PostMapping("/breeds")
    public Map<String, Object> createBreed(@RequestBody PetBreed body, HttpSession session) {
        requireAdmin(session);
        PetBreed b = petAdminService.createBreed(body);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        m.put("breed", b);
        return m;
    }

    @PutMapping("/breeds/{id}")
    public Map<String, Object> updateBreed(@PathVariable Long id, @RequestBody PetBreed patch,
                                           HttpSession session) {
        requireAdmin(session);
        PetBreed b = petAdminService.updateBreed(id, patch);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        m.put("breed", b);
        return m;
    }

    @DeleteMapping("/breeds/{id}")
    public Map<String, Object> deleteBreed(@PathVariable Long id, HttpSession session) {
        requireAdmin(session);
        petAdminService.deleteBreed(id);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        return m;
    }

    /* ================= 内部辅助 ================= */

    /** 会话守卫：未登录后台时抛 401，由 GlobalExceptionHandler 统一返回 */
    private static String requireAdmin(HttpSession session) {
        Object admin = session == null ? null : session.getAttribute(SESSION_ADMIN);
        if (admin == null) {
            throw new AuthException("unauthorized", "请先登录后台管理系统", 401);
        }
        return String.valueOf(admin);
    }
}
