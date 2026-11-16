package com.fenglin.springboottest.dto;

/**
 * 评论请求体：POST /api/content/posts/{id}/comments
 * author / authorName 由前端透传当前用户（2026-11 起身份取会话，前端传参仅作展示昵称兜底）。
 * replyToId 非空 = 楼中楼回复（回复目标必须是同一篇帖子的评论）。
 */
public class CommentRequest {

    private String content;
    private String author;
    private String authorName;
    private Long replyToId;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public Long getReplyToId() { return replyToId; }
    public void setReplyToId(Long replyToId) { this.replyToId = replyToId; }
}
