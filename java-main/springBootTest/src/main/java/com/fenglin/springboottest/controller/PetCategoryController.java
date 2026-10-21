package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.service.PetCategoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 萌宠分类 / 品种图鉴接口（供前端首页「萌宠分类」与分类详情页使用）：
 *
 *  GET /api/content/categories          -> { ok, items: [Category] }
 *  GET /api/content/categories/{id}     -> { ok, category: Category }
 *
 * Category: { id, name, icon, color, image, breeds: [{ name, image, desc }] }
 *
 * 说明：只读接口，无需登录态。前端 petData.js 会优先调用本接口，
 * 接口不可用时回退到内置静态数据，保证纯前端演示也能正常显示。
 */
@RestController
@RequestMapping("/api/content")
public class PetCategoryController {

    private final PetCategoryService petCategoryService;

    public PetCategoryController(PetCategoryService petCategoryService) {
        this.petCategoryService = petCategoryService;
    }

    /** 分类列表（含品种），一次返回全部 */
    @GetMapping("/categories")
    public Map<String, Object> list() {
        List<Map<String, Object>> items = petCategoryService.listAll();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        m.put("items", items);
        return m;
    }

    /** 单个分类详情 */
    @GetMapping("/categories/{id}")
    public Map<String, Object> detail(@PathVariable String id) {
        Map<String, Object> category = petCategoryService.detail(id);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", category != null);
        m.put("category", category);
        return m;
    }
}
