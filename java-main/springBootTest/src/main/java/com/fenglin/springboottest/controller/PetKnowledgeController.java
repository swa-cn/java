package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.service.KnowledgeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 知识课堂读接口（/api/content/knowledge*）。
 * 只读查询，无需登录态，与分类/帖子同属 content 域。
 *
 *  GET /api/content/knowledge        -> { ok, items: [Article] }
 *  GET /api/content/knowledge/{id}   -> { ok, article: Article }（浏览数 +1）
 *
 * Article: { id, cat, title, summary, tags: [], sections: [{h,p}], views, createdAt }
 * 管理端维护（增删改）见 AdminController 的 /api/admin/knowledge*。
 */
@RestController
@RequestMapping("/api/content")
public class PetKnowledgeController {

    private final KnowledgeService knowledgeService;

    public PetKnowledgeController(KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    /** 文章列表（含正文，数据量小直接全量返回，前端本地搜索） */
    @GetMapping("/knowledge")
    public Map<String, Object> list() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        m.put("items", knowledgeService.list());
        return m;
    }

    /** 文章详情：每次打开浏览数 +1 */
    @GetMapping("/knowledge/{id}")
    public Map<String, Object> read(@PathVariable Long id) {
        Map<String, Object> article = knowledgeService.read(id);
        if (article == null) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error", "notfound");
            err.put("message", "文章不存在");
            return err;
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        m.put("article", article);
        return m;
    }
}
