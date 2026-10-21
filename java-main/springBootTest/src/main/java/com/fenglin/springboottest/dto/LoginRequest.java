package com.fenglin.springboottest.dto;

/** 登录请求体 */
public class LoginRequest {
    private String account;
    private String password;
    private Boolean remember;

    public String getAccount() { return account; }
    public void setAccount(String account) { this.account = account; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Boolean getRemember() { return remember; }
    public void setRemember(Boolean remember) { this.remember = remember; }
}
