package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 帖子评论（表 pet_post_comment）。
 */
@TableName("pet_post_comment")
public class PetPostComment {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 帖子 id */
    private Long postId;

    /** 评论人登录名 */
    private String author;

    /** 评论人昵称 */
    private String authorName;

    /** 评论内容（<=300） */
    private String content;

    /** 回复的目标评论 id（null = 顶层评论；楼中楼） */
    private Long replyToId;

    /** 被回复人的登录名（冗余存储，用于前端展示「回复 @xxx」） */
    private String replyToAuthor;

    /** 评论时间 */
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Long getReplyToId() { return replyToId; }
    public void setReplyToId(Long replyToId) { this.replyToId = replyToId; }

    public String getReplyToAuthor() { return replyToAuthor; }
    public void setReplyToAuthor(String replyToAuthor) { this.replyToAuthor = replyToAuthor; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

}