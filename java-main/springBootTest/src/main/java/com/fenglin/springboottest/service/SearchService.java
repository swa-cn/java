package com.fenglin.springboottest.service;

import java.util.Map;

/**
 * 全局搜索业务接口（首页搜索框 → 搜索结果页）。
 *
 * 一次请求聚合四类结果，前端按「种类」分组渲染，避免一个关键词发四次请求：
 * {
 *   query, total,
 *   counts: { categories, breeds, posts, users },
 *   categories: [{ id, name, icon, color, image, breedCount }],
 *   breeds:     [{ name, image, desc, categoryId, categoryName, categoryColor }],
 *   posts:      [{ id, title, summary, cover, category, categoryName, author, authorName, createdAt }],
 *   users:      [{ key, name }]
 * }
 *
 * 匹配口径（大小写不敏感，均为「包含」匹配）：
 *  - 分类：分类名命中（如「猫」→ 猫类）
 *  - 品种：品种名 / 品种简介命中，或所属分类名命中（搜「猫」时猫类下所有品种都算相关）
 *  - 动态：标题 / 正文 / 标签命中，或所属分类命中
 *  - 用户：登录名 / 昵称命中
 *
 * 排序：按相关度（完全相等 > 前缀 > 包含 > 简介/正文命中）降序，同级保持原有顺序
 * （分类/品种按 sort_no，动态按发布时间倒序）。
 */
public interface SearchService {

    /**
     * 聚合搜索。
     *
     * @param keyword       关键词（空白时返回空结果，不抛异常）
     * @param limitPerGroup 每组最多返回多少条（<=0 时用默认值 20）
     */
    Map<String, Object> search(String keyword, int limitPerGroup);
}
