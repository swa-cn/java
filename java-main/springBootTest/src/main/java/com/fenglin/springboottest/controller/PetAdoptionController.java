package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.service.PetAdoptionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 萌宠领养养成接口（供前端 login-vue 领养模块使用）：
 *
 *  GET  /api/adoption/state?owner=             -> dashboard（物种/资源/宠物/任务/互动/赠送/好友）
 *  POST /api/adoption/adopt      { owner, speciesId, name? }  -> dashboard
 *  POST /api/adoption/hatch      { owner, petId }             -> dashboard
 *  POST /api/adoption/feed       { owner, petId }             -> dashboard + gain
 *  POST /api/adoption/interact   { owner, petId }             -> dashboard + gain
 *  POST /api/adoption/task/complete { owner, taskId }         -> dashboard
 *  POST /api/adoption/task/claim    { owner, taskId }         -> dashboard + reward
 *  POST /api/adoption/gift/claim    { owner, friendKey }      -> dashboard + reward
 *
 * 写操作返回最新 dashboard，便于前端一次刷新。失败返回 { ok:false, reason }。
 * owner 为当前登录用户名（与项目现有内容接口一致，由客户端传入）。
 */
@RestController
@RequestMapping("/api/adoption")
public class PetAdoptionController {

    private final PetAdoptionService service;

    public PetAdoptionController(PetAdoptionService service) {
        this.service = service;
    }

    @GetMapping("/state")
    public Map<String, Object> state(@RequestParam String owner) {
        return service.dashboard(owner);
    }

    @PostMapping("/adopt")
    public Map<String, Object> adopt(@RequestBody Map<String, Object> body) {
        return service.adopt(str(body, "owner"), str(body, "speciesId"), str(body, "name"));
    }

    @PostMapping("/hatch")
    public Map<String, Object> hatch(@RequestBody Map<String, Object> body) {
        return service.hatch(str(body, "owner"), str(body, "petId"));
    }

    @PostMapping("/feed")
    public Map<String, Object> feed(@RequestBody Map<String, Object> body) {
        return service.feed(str(body, "owner"), str(body, "petId"));
    }

    @PostMapping("/interact")
    public Map<String, Object> interact(@RequestBody Map<String, Object> body) {
        return service.interact(str(body, "owner"), str(body, "petId"));
    }

    @PostMapping("/task/complete")
    public Map<String, Object> completeTask(@RequestBody Map<String, Object> body) {
        return service.completeTask(str(body, "owner"), str(body, "taskId"));
    }

    @PostMapping("/task/claim")
    public Map<String, Object> claimTask(@RequestBody Map<String, Object> body) {
        return service.claimTask(str(body, "owner"), str(body, "taskId"));
    }

    @PostMapping("/gift/claim")
    public Map<String, Object> claimGift(@RequestBody Map<String, Object> body) {
        return service.claimGift(str(body, "owner"), str(body, "friendKey"));
    }

    @PostMapping("/rename")
    public Map<String, Object> rename(@RequestBody Map<String, Object> body) {
        return service.rename(str(body, "owner"), str(body, "petId"), str(body, "name"));
    }

    private static String str(Map<String, Object> body, String key) {
        Object v = body == null ? null : body.get(key);
        return v == null ? null : String.valueOf(v);
    }
}
