package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 关注关系（表 pet_follow）。follower 关注 followee，单向；同一对唯一。
 */
@TableName("pet_follow")
public class PetFollow {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关注发起人 */
    private String followerKey;

    /** 被关注人 */
    private String followeeKey;

    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFollowerKey() { return followerKey; }
    public void setFollowerKey(String followerKey) { this.followerKey = followerKey; }

    public String getFolloweeKey() { return followeeKey; }
    public void setFolloweeKey(String followeeKey) { this.followeeKey = followeeKey; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
