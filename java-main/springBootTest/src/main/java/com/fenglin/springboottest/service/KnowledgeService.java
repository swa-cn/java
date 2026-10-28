package com.fenglin.springboottest.service;

import java.util.List;
import java.util.Map;

/**
 * 知识课堂服务（pet_knowledge 表）。
 *
 * 读接口（/api/content/knowledge*）对所有访客开放：
 *  GET /api/content/knowledge        文章列表（含解析好的 tags / sections）
 *  GET /api/content/knowledge/{id}    文章详情（浏览数 +1）
 *
 * 管理接口（/api/admin/knowledge*）由 AdminController 调用（需后台会话）。
 */
public interface KnowledgeService {

    /** 文章列表（tags/sections 已解析为 JSON 结构），按 id 正序 */
    List<Map<String, Object>> list();

    /** 文章详情：返回解析后的结构，并把 views + 1（不存在返回 null） */
    Map<String, Object> read(Long id);

    /** 新增。body: { cat, title, summary, tags:[]|逗号串, sections:[{h,p}] } */
    Map<String, Object> create(Map<String, Object> body);

    /** 修改（部分字段）。patch 支持同 create 的全部字段，可不传的不改 */
    Map<String, Object> update(Long id, Map<String, Object> patch);

    /** 删除 */
    void delete(Long id);
}
