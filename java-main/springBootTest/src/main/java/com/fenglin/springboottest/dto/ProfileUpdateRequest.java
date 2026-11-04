package com.fenglin.springboottest.dto;

import java.util.List;

/**
 * 「设置 → 我的资料」保存请求体：POST /api/auth/profile。
 * username 由前端透传（本项目演示约定：与好友/私信模块的 owner_key 同风格）。
 * birthday 为 yyyy-MM-dd 字符串（可空）；tags 最多 6 个、每个最长 8 字（前端已限制，后端兜底）。
 */
public class ProfileUpdateRequest {
    private String username;
    private String nickname;
    private String email;
    private String birthday;
    private List<String> tags;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getBirthday() { return birthday; }
    public void setBirthday(String birthday) { this.birthday = birthday; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
}
