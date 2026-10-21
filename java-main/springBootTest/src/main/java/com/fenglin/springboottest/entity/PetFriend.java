package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 好友关系（表 pet_friend）。
 *
 * 设计：一条记录代表「owner 与 friend 的关系」。
 *  - status = 'friend'  已是好友
 *  - status = 'pending' owner 发出申请，等待 friend 同意
 * 唯一键保证同一对用户不会重复添加。
 */
@TableName("pet_friend")
public class PetFriend {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发起方登录名（谁的列表里出现这条关系） */
    private String ownerKey;

    /** 对方登录名 */
    private String friendKey;

    /** 对方昵称（展示用，避免再查 app_user） */
    private String friendName;

    /** 关系状态：friend / pending */
    private String status;

    /** 来源：post / search / profile / manual */
    private String source;

    /** 申请时间 */
    private LocalDateTime createdAt;

    /** 成为好友的时间 */
    private LocalDateTime acceptedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOwnerKey() { return ownerKey; }
    public void setOwnerKey(String ownerKey) { this.ownerKey = ownerKey; }

    public String getFriendKey() { return friendKey; }
    public void setFriendKey(String friendKey) { this.friendKey = friendKey; }

    public String getFriendName() { return friendName; }
    public void setFriendName(String friendName) { this.friendName = friendName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getAcceptedAt() { return acceptedAt; }
    public void setAcceptedAt(LocalDateTime acceptedAt) { this.acceptedAt = acceptedAt; }
}
