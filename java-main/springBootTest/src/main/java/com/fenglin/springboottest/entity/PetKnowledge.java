package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 知识课堂文章（pet_knowledge 表）。
 *
 * cat：文章所属分类 id（cat/dog/small/bird/fish），'all' 表示通用。
 * tags / content：以 JSON 字符串存储 ——
 *   tags   -> ["疫苗","健康"]
 *   content-> [{"h":"标题","p":"正文"}]
 * 取出时由服务层解析为结构化字段再返回给前端。
 */
@TableName("pet_knowledge")
public class PetKnowledge {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 分类 id：cat / dog / small / bird / fish / all（通用） */
    private String cat;

    /** 标题 */
    private String title;

    /** 一句话摘要 */
    private String summary;

    /** 标签 JSON 数组字符串 */
    private String tags;

    /** 正文 JSON：[{h,p}] */
    private String content;

    /** 浏览数（打开详情 +1） */
    private Long views;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCat() { return cat; }
    public void setCat(String cat) { this.cat = cat; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Long getViews() { return views; }
    public void setViews(Long views) { this.views = views; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
