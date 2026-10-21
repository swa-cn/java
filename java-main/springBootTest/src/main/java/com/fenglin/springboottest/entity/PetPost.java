package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 萌宠帖子（表 pet_post）。
 */
@TableName("pet_post")
public class PetPost {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 标题（选填，<=80） */
    private String title;

    /** 正文 */
    private String content;

    /** 分类 id：cat/dog/small/bird/fish */
    private String category;

    /** 标签，逗号分隔 */
    private String tags;

    /** 作者登录名 */
    private String author;

    /** 作者昵称（展示用） */
    private String authorName;

    /** 发布时间 */
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

}