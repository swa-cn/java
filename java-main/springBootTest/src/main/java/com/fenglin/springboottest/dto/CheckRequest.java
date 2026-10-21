package com.fenglin.springboottest.dto;

/** 用户名占用检测请求体 */
public class CheckRequest {
    private String username;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
