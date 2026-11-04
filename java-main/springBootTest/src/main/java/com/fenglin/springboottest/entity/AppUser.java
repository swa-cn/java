package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户表（注册用户）。表名 app_user 以避免与 MySQL 关键字冲突。
 */
@TableName("app_user")
public class AppUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 登录用户名：3-20 位字母/数字/下划线，唯一 */
    private String username;

    /** 邮箱：唯一 */
    private String email;

    /** 昵称（选填） */
    private String nickname;

    /** 生日（选填，yyyy-MM-dd）——「设置 → 我的资料」持久化字段 */
    private LocalDate birthday;

    /** 个性标签（选填）：JSON 数组字符串，如 ["猫奴","吃货"]，最多 6 个 × 8 字 */
    private String tags;

    /** BCrypt 哈希后的密码（60 字符，预留 100） */
    private String password;

    /** 权限：0=管理员，1=默认（普通用户）。注册用户一律 1 */
    private Integer role;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public LocalDate getBirthday() { return birthday; }
    public void setBirthday(LocalDate birthday) { this.birthday = birthday; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Integer getRole() { return role; }
    public void setRole(Integer role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
