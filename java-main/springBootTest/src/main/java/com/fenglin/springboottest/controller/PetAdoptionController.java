package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.auth.CurrentUser;
import com.fenglin.springboottest.service.PetAdoptionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 萌宠领养养成接口（供前端 login-vue 领养模块使用）：
 *
 *  GET  /api/adoption/state              -> dashboard（物种/资源/宠物/任务/互动/赠送/好友）
 *  POST /api/adoption/adopt              { speciesId, name? }      -> dashboard
 *  POST /api/adoption/hatch              { petId }                 -> dashboard
 *  POST /api/adoption/feed               { petId }                 -> dashboard + gain
 *  POST /api/adoption/interact           { petId }                 -> dashboard + gain
 *  POST /api/adoption/task/complete      { taskId }                -> dashboard
 *  POST /api/adoption/task/claim         { taskId }                -> dashboard + reward
 *  POST /api/adoption/gift/send          { friendKey }             -> dashboard（给好友赠送）
 *  POST /api/adoption/gift/claim         { giftId }                -> dashboard + reward（领取好友赠送）
 *  POST /api/adoption/rename             { petId, name }           -> dashboard
 *  GET  /api/adoption/ranking            -> 排行榜（亲密度/活跃用户）
 *  GET  /api/adoption/achievements       -> 成就徽章（含 claimed / reward）
 *  POST /api/adoption/achievement/claim  { achievementId } -> dashboard + reward（领取成就奖励）
 *
 * 写操作返回最新 dashboard，便于前端一次刷新。失败返回 { ok:false, reason }。
 * 身份（2026-11 起）：owner 一律取登录会话用户，前端传参仅作兼容、忽略 —— 养宠数据只能操作自己的。
 */
@RestController
@RequestMapping("/api/adoption")
public class PetAdoptionController {

    private final PetAdoptionService service;

    public PetAdoptionController(PetAdoptionService service) {
        this.service = service;
    }

    @GetMapping("/state")
    public Map<String, Object> state(HttpServletRequest request) {
        return service.dashboard(CurrentUser.require(request));
    }

    @PostMapping("/adopt")
    public Map<String, Object> adopt(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        return service.adopt(CurrentUser.require(request), str(body, "speciesId"), str(body, "name"));
    }

    @PostMapping("/hatch")
    public Map<String, Object> hatch(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        return service.hatch(CurrentUser.require(request), str(body, "petId"));
    }

    @PostMapping("/feed")
    public Map<String, Object> feed(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        return service.feed(CurrentUser.require(request), str(body, "petId"));
    }

    @PostMapping("/interact")
    public Map<String, Object> interact(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        return service.interact(CurrentUser.require(request), str(body, "petId"));
    }

    @PostMapping("/task/complete")
    public Map<String, Object> completeTask(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        return service.completeTask(CurrentUser.require(request), str(body, "taskId"));
    }

    @PostMapping("/task/claim")
    public Map<String, Object> claimTask(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        return service.claimTask(CurrentUser.require(request), str(body, "taskId"));
    }

    @PostMapping("/gift/send")
    public Map<String, Object> sendGift(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        return service.sendGift(CurrentUser.require(request), str(body, "friendKey"));
    }

    @PostMapping("/gift/claim")
    public Map<String, Object> claimGift(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Object id = body == null ? null : body.get("giftId");
        Long giftId = null;
        if (id != null) {
            try {
                giftId = Long.valueOf(String.valueOf(id));
            } catch (NumberFormatException e) {
                giftId = null;
            }
        }
        return service.claimGift(CurrentUser.require(request), giftId);
    }

    @PostMapping("/rename")
    public Map<String, Object> rename(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        return service.rename(CurrentUser.require(request), str(body, "petId"), str(body, "name"));
    }

    /** 排行榜：GET /api/adoption/ranking -> { ok, intimacyTop, activeTop } */
    @GetMapping("/ranking")
    public Map<String, Object> ranking() {
        return service.ranking();
    }

    /** 成就徽章：GET /api/adoption/achievements -> { ok, achievements } */
    @GetMapping("/achievements")
    public Map<String, Object> achievements(HttpServletRequest request) {
        return service.achievements(CurrentUser.require(request));
    }

    /** 领取成就奖励：POST /api/adoption/achievement/claim { achievementId } -> dashboard + reward */
    @PostMapping("/achievement/claim")
    public Map<String, Object> claimAchievement(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        return service.claimAchievement(CurrentUser.require(request), str(body, "achievementId"));
    }

    private static String str(Map<String, Object> body, String key) {
        Object v = body == null ? null : body.get(key);
        return v == null ? null : String.valueOf(v);
    }
}
