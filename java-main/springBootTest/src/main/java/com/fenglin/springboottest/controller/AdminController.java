package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.dto.AdminLoginRequest;
import com.fenglin.springboottest.entity.AppUser;
import com.fenglin.springboottest.entity.PetBreed;
import com.fenglin.springboottest.entity.PetCategory;
import com.fenglin.springboottest.exception.AuthException;
import com.fenglin.springboottest.service.AdminUserService;
import com.fenglin.springboottest.service.AuthService;
import com.fenglin.springboottest.service.KnowledgeService;
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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
 *
 * 用户管理（app_user 表，不改数据库直接改权限的既有用法，两边并行可用）：
 *  GET    /api/admin/users?keyword=      用户列表（不含密码）
 *  POST   /api/admin/users               新增用户/管理员
 *  PUT    /api/admin/users/{id}          修改昵称 / 重置密码 / 调整权限
 *  DELETE /api/admin/users/{id}          删除用户
 *
 * 知识课堂管理（pet_knowledge 表）：
 *  GET    /api/admin/knowledge           文章列表
 *  POST   /api/admin/knowledge           新增文章
 *  PUT    /api/admin/knowledge/{id}      修改文章
 *  DELETE /api/admin/knowledge/{id}      删除文章
 *
 * 权限模型：admin.username（默认 admin）为超级管理员，权限最大；
 * 只有它能新增/提升/降级/删除「管理员」，其他管理员只能管理普通用户。
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    /** 会话里标记管理员的属性名 */
    public static final String SESSION_ADMIN = "admin.user";

    /** 权限常量：app_user.role —— 0=管理员，1=默认 */
    public static final int ROLE_ADMIN = 0;

    /** 超级管理员账号（最大权限，只有它能任免其他管理员） */
    @Value("${admin.username:admin}")
    private String superAdminUsername;

    private final AuthService authService;
    private final PetAdminService petAdminService;
    private final AdminUserService adminUserService;
    private final KnowledgeService knowledgeService;

    public AdminController(AuthService authService, PetAdminService petAdminService,
                           AdminUserService adminUserService, KnowledgeService knowledgeService) {
        this.authService = authService;
        this.petAdminService = petAdminService;
        this.adminUserService = adminUserService;
        this.knowledgeService = knowledgeService;
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

    /* ================= 用户管理 ================= */

    /** 用户列表：keyword 可选，按用户名/昵称模糊匹配。是否超级管理员由前端根据登录账号判断 */
    @GetMapping("/users")
    public Map<String, Object> listUsers(@RequestParam(required = false) String keyword,
                                         HttpSession session) {
        requireAdmin(session);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        m.put("items", adminUserService.listUsers(keyword));
        return m;
    }

    /** 新增用户。body: { username, nickname, password, role }；role=0（管理员）仅超级管理员可用 */
    @PostMapping("/users")
    public Map<String, Object> createUser(@RequestBody Map<String, Object> body, HttpSession session) {
        String admin = requireAdmin(session);
        Map<String, Object> user = adminUserService.create(body, isSuperAdmin(admin));
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        m.put("user", user);
        return m;
    }

    /** 修改用户。patch: { nickname?, password?, role? }（字段可选，只传要改的） */
    @PutMapping("/users/{id}")
    public Map<String, Object> updateUser(@PathVariable Long id,
                                          @RequestBody Map<String, Object> patch,
                                          HttpSession session) {
        String admin = requireAdmin(session);
        Map<String, Object> user = adminUserService.update(id, patch, isSuperAdmin(admin));
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        m.put("user", user);
        return m;
    }

    /** 删除用户。不能删自己 / 不能删 admin 账号 / 删管理员仅超级管理员可用 */
    @DeleteMapping("/users/{id}")
    public Map<String, Object> deleteUser(@PathVariable Long id, HttpSession session) {
        String admin = requireAdmin(session);
        adminUserService.delete(id, admin, isSuperAdmin(admin));
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        return m;
    }

    /* ================= 知识课堂管理 ================= */

    /**
     * 文章列表。keyword 可选，在标题 / 摘要 / 标签 / 正文里做全文匹配（数据量小，粗暴有效）。
     */
    @GetMapping("/knowledge")
    public Map<String, Object> listKnowledge(@RequestParam(required = false) String keyword,
                                             HttpSession session) {
        requireAdmin(session);
        List<Map<String, Object>> items = knowledgeService.list();
        if (keyword != null && !keyword.isBlank()) {
            String k = keyword.trim().toLowerCase();
            items = items.stream()
                    .filter(a -> haystack(a).contains(k))
                    .collect(Collectors.toList());
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        m.put("items", items);
        return m;
    }

    /** 把一篇文章的标题/摘要/标签/正文拼成一个可检索字符串 */
    private String haystack(Map<String, Object> a) {
        StringBuilder sb = new StringBuilder();
        sb.append(str(a.get("title"))).append('\n')
          .append(str(a.get("summary"))).append('\n')
          .append(str(a.get("tags"))).append('\n');
        Object sections = a.get("sections");
        if (sections instanceof Collection<?> list) {
            for (Object o : list) {
                if (o instanceof Map<?, ?> s) {
                    sb.append(str(s.get("h"))).append(' ').append(str(s.get("p"))).append('\n');
                }
            }
        }
        return sb.toString().toLowerCase();
    }

    private String str(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    /** 新增文章。body: { cat, title, summary, tags, sections:[{h,p}] } */
    @PostMapping("/knowledge")
    public Map<String, Object> createKnowledge(@RequestBody Map<String, Object> body, HttpSession session) {
        requireAdmin(session);
        Map<String, Object> article = knowledgeService.create(body);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        m.put("article", article);
        return m;
    }

    /** 修改文章（部分字段） */
    @PutMapping("/knowledge/{id}")
    public Map<String, Object> updateKnowledge(@PathVariable Long id,
                                                @RequestBody Map<String, Object> patch,
                                                HttpSession session) {
        requireAdmin(session);
        Map<String, Object> article = knowledgeService.update(id, patch);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        m.put("article", article);
        return m;
    }

    @DeleteMapping("/knowledge/{id}")
    public Map<String, Object> deleteKnowledge(@PathVariable Long id, HttpSession session) {
        requireAdmin(session);
        knowledgeService.delete(id);
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

    /** 超级管理员判定：当前登录账号 == 配置的 admin.username（默认 admin） */
    private boolean isSuperAdmin(String username) {
        return superAdminUsername != null && superAdminUsername.equalsIgnoreCase(username);
    }
}
