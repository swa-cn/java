package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.service.CaptchaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class CaptchaController {

    private final CaptchaService captchaService;

    public CaptchaController(CaptchaService captchaService) {
        this.captchaService = captchaService;
    }

    /**
     * 获取图形验证码：GET /api/auth/captcha
     * 返回 { ok, captchaId, image }，image 为 data:image/png;base64,... 可直接放进 <img src>
     * 验证码 5 分钟内有效、一次性（校验一次即失效，无论对错）。
     */
    @GetMapping("/captcha")
    public Map<String, Object> captcha() {
        Map<String, String> data = captchaService.generate();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        m.put("captchaId", data.get("captchaId"));
        m.put("image", data.get("image"));
        m.put("ttl", 5 * 60 * 1000L);
        return m;
    }
}
