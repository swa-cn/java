package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.auth.CurrentUser;
import com.fenglin.springboottest.service.FollowService;
import com.fenglin.springboottest.service.FriendService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 好友接口（与前端 login-vue/src/api/friend.js 的约定一一对应）：
 *
 *  GET    /api/content/friends?status=            -> { ok, items: [Relation] }
 *  GET    /api/content/friends/incoming           -> { ok, items: [Relation] }  别人发给我的申请
 *  GET    /api/content/friends/relation?target=   -> { ok, status }
 *  GET    /api/content/friends/search?keyword=    -> { ok, items: [User] }      按账号搜索加好友
 *  POST   /api/content/friends                    { target, name, from } -> { ok, relation }
 *  POST   /api/content/friends/accept             { target } -> { ok, relation }
 *  DELETE /api/content/friends?target=            -> { ok }
 *
 * Relation: { key, name, status:'friend'|'pending', from, createdAt, incoming }
 * User    : { key, name, status }   status 为 relative to 当前登录用户
 *
 * 身份（2026-11 起）：owner 一律取登录会话用户，前端传参仅作兼容、忽略 —— 好友关系只能操作自己的。
 */
@RestController
@RequestMapping("/api/content")
public class FriendController {

    private final FriendService friendService;
    private final FollowService followService;

    public FriendController(FriendService friendService, FollowService followService) {
        this.friendService = friendService;
        this.followService = followService;
    }

    /** 好友列表（status 可选：friend / pending） */
    @GetMapping("/friends")
    public Map<String, Object> list(@RequestParam(required = false) String status,
                                    HttpServletRequest request) {
        List<Map<String, Object>> items = friendService.list(CurrentUser.require(request), status);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        m.put("items", items);
        return m;
    }

    /** 别人发给我、等待我处理的申请 */
    @GetMapping("/friends/incoming")
    public Map<String, Object> incoming(HttpServletRequest request) {
        List<Map<String, Object>> items = friendService.listIncoming(CurrentUser.require(request));
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        m.put("items", items);
        return m;
    }

    /** 与某人的关系状态 */
    @GetMapping("/friends/relation")
    public Map<String, Object> relation(@RequestParam(required = false) String target,
                                        HttpServletRequest request) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        m.put("status", friendService.relation(CurrentUser.require(request), target));
        return m;
    }

    /** 按用户名 / 昵称搜索用户（加好友用） */
    @GetMapping("/friends/search")
    public Map<String, Object> search(@RequestParam(required = false) String keyword,
                                      HttpServletRequest request) {
        List<Map<String, Object>> items = friendService.searchUsers(CurrentUser.require(request), keyword);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        m.put("items", items);
        return m;
    }

    /** 发送好友申请（申请人 = 登录用户；name/from 仅作展示） */
    @PostMapping("/friends")
    public Map<String, Object> add(@RequestBody Map<String, String> body, HttpServletRequest request) {
        Map<String, Object> relation = friendService.add(
                CurrentUser.require(request), body.get("target"), body.get("name"), body.get("from"));
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", relation != null);
        m.put("relation", relation);
        return m;
    }

    /** 同意好友申请（操作人 = 登录用户） */
    @PostMapping("/friends/accept")
    public Map<String, Object> accept(@RequestBody Map<String, String> body, HttpServletRequest request) {
        Map<String, Object> relation = friendService.accept(CurrentUser.require(request), body.get("target"));
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", relation != null);
        m.put("relation", relation);
        return m;
    }

    /** 删除好友 / 撤回申请 / 拒绝申请（操作人 = 登录用户） */
    @DeleteMapping("/friends")
    public Map<String, Object> remove(@RequestParam(required = false) String target,
                                      HttpServletRequest request) {
        friendService.remove(CurrentUser.require(request), target);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        return m;
    }

    /* ================= 关注 / 粉丝 ================= */

    /** 关注 / 取消关注（切换）：POST /api/content/users/{key}/follow，关注人 = 登录用户 */
    @PostMapping("/users/{key}/follow")
    public Map<String, Object> follow(@PathVariable String key, HttpServletRequest request) {
        Map<String, Object> result = followService.toggle(CurrentUser.require(request), key);
        Map<String, Object> m = new LinkedHashMap<>();
        m.putAll(result);
        return m;
    }

    /** 关注/粉丝数量：GET /api/content/users/{key}/follow/counts -> { ok, followers, following_count } */
    @GetMapping("/users/{key}/follow/counts")
    public Map<String, Object> followCounts(@PathVariable String key) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        m.putAll(followService.counts(key));
        return m;
    }

    /** 我关注的人：GET /api/content/users/{key}/following -> { ok, items } */
    @GetMapping("/users/{key}/following")
    public Map<String, Object> following(@PathVariable String key) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        m.put("items", followService.following(key));
        return m;
    }

    /** 关注我的人（粉丝）：GET /api/content/users/{key}/followers -> { ok, items } */
    @GetMapping("/users/{key}/followers")
    public Map<String, Object> followers(@PathVariable String key) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        m.put("items", followService.followers(key));
        return m;
    }
}
