package com.fenglin.springboottest.dto;

/**
 * 评论请求体：POST /api/content/posts/{id}/comments
 * author / authorName 由前端透传当前登录用户（演示级实现，未绑定会话）。
 */
public class CommentRequest {

    private String content;
    private String author;
    private String authorName;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
}
