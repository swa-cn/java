package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 帖子收藏（表 pet_post_favorite）。一人对一帖只能收藏一次（唯一索引兜底）。
 */
@TableName("pet_post_favorite")
public class PetPostFavorite {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 帖子 id */
    private Long postId;

    /** 收藏人登录名 */
    private String userKey;

    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }

    public String getUserKey() { return userKey; }
    public void setUserKey(String userKey) { this.userKey = userKey; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
