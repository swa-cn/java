package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.service.FriendService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
 *  GET    /api/content/friends?owner=&status=           -> { ok, items: [Relation] }
 *  GET    /api/content/friends/incoming?owner=          -> { ok, items: [Relation] }  别人发给我的申请
 *  GET    /api/content/friends/relation?owner=&target=  -> { ok, status }
 *  GET    /api/content/friends/search?owner=&keyword=   -> { ok, items: [User] }      按账号搜索加好友
 *  POST   /api/content/friends                          { owner, target, name, from } -> { ok, relation }
 *  POST   /api/content/friends/accept                   { owner, target } -> { ok, relation }
 *  DELETE /api/content/friends?owner=&target=            -> { ok }
 *
 * Relation: { key, name, status:'friend'|'pending', from, createdAt, incoming }
 * User    : { key, name, status }   status 为 relative to 当前 owner
 */
@RestController
@RequestMapping("/api/content")
public class FriendController {

    private final FriendService friendService;

    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    /** 好友列表（status 可选：friend / pending） */
    @GetMapping("/friends")
    public Map<String, Object> list(@RequestParam(required = false) String owner,
                                    @RequestParam(required = false) String status) {
        List<Map<String, Object>> items = friendService.list(owner, status);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        m.put("items", items);
        return m;
    }

    /** 别人发给我、等待我处理的申请 */
    @GetMapping("/friends/incoming")
    public Map<String, Object> incoming(@RequestParam(required = false) String owner) {
        List<Map<String, Object>> items = friendService.listIncoming(owner);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        m.put("items", items);
        return m;
    }

    /** 与某人的关系状态 */
    @GetMapping("/friends/relation")
    public Map<String, Object> relation(@RequestParam(required = false) String owner,
                                        @RequestParam(required = false) String target) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        m.put("status", friendService.relation(owner, target));
        return m;
    }

    /** 按用户名 / 昵称搜索用户（加好友用） */
    @GetMapping("/friends/search")
    public Map<String, Object> search(@RequestParam(required = false) String owner,
                                      @RequestParam(required = false) String keyword) {
        List<Map<String, Object>> items = friendService.searchUsers(owner, keyword);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        m.put("items", items);
        return m;
    }

    /** 发送好友申请 */
    @PostMapping("/friends")
    public Map<String, Object> add(@RequestBody Map<String, String> body) {
        Map<String, Object> relation = friendService.add(
                body.get("owner"), body.get("target"), body.get("name"), body.get("from"));
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", relation != null);
        m.put("relation", relation);
        return m;
    }

    /** 同意好友申请 */
    @PostMapping("/friends/accept")
    public Map<String, Object> accept(@RequestBody Map<String, String> body) {
        Map<String, Object> relation = friendService.accept(body.get("owner"), body.get("target"));
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", relation != null);
        m.put("relation", relation);
        return m;
    }

    /** 删除好友 / 撤回申请 / 拒绝申请 */
    @DeleteMapping("/friends")
    public Map<String, Object> remove(@RequestParam(required = false) String owner,
                                      @RequestParam(required = false) String target) {
        friendService.remove(owner, target);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        return m;
    }
}
