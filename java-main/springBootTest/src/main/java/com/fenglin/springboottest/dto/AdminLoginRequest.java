package com.fenglin.springboottest.dto;

/**
 * 后台管理员登录请求体。
 *
 * 复用 /api/auth/login 的账号校验逻辑（BCrypt 比对 app_user 表），
 * 再额外要求账号必须是配置的管理员账号（admin.username）。
 */
public class AdminLoginRequest {

    /** 管理员账号（用户名） */
    private String account;

    /** 密码（明文传输到后端做 BCrypt 比对） */
    private String password;

    public String getAccount() { return account; }
    public void setAccount(String account) { this.account = account; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
