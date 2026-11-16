package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 帖子举报（表 pet_post_report）。
 * status: pending=待处理 / dismissed=驳回（内容无问题） / deleted=已删帖处理。
 * 同一人对同一帖只能举报一次（唯一索引兜底）。
 */
@TableName("pet_post_report")
public class PetPostReport {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 被举报的帖子 id */
    private Long postId;

    /** 举报人登录名 */
    private String reporter;

    /** 举报理由（<=200） */
    private String reason;

    /** pending / dismissed / deleted */
    private String status;

    private LocalDateTime createdAt;

    /** 处理时间 */
    private LocalDateTime handledAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }

    public String getReporter() { return reporter; }
    public void setReporter(String reporter) { this.reporter = reporter; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getHandledAt() { return handledAt; }
    public void setHandledAt(LocalDateTime handledAt) { this.handledAt = handledAt; }
}
