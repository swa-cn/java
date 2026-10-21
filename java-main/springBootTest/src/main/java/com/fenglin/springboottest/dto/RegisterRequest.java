package com.fenglin.springboottest.dto;

/**
 * 注册请求体（不收集邮箱，人机校验用图形验证码）。
 * captchaId  来自 GET /api/auth/captcha 返回；
 * captchaCode 为用户看图输入的 4 位验证码（一次性，5 分钟内有效）。
 */
public class RegisterRequest {
    private String username;
    private String password;
    private String nickname;
    private String captchaId;
    private String captchaCode;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getCaptchaId() { return captchaId; }
    public void setCaptchaId(String captchaId) { this.captchaId = captchaId; }

    public String getCaptchaCode() { return captchaCode; }
    public void setCaptchaCode(String captchaCode) { this.captchaCode = captchaCode; }
}
