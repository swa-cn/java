package com.fenglin.springboottest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fenglin.springboottest.entity.PetKnowledge;
import com.fenglin.springboottest.exception.AuthException;
import com.fenglin.springboottest.mapper.PetKnowledgeMapper;
import com.fenglin.springboottest.service.KnowledgeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 知识课堂实现。
 *
 * tags / content 以 JSON 字符串存库（TEXT），读时解析、写时序列化，
 * 前端始终拿到结构化的 tags:[] 与 sections:[{h,p}]。
 */
@Service
public class KnowledgeServiceImpl implements KnowledgeService {

    private final PetKnowledgeMapper knowledgeMapper;
    private static final ObjectMapper JSON = new ObjectMapper();

    public KnowledgeServiceImpl(PetKnowledgeMapper knowledgeMapper) {
        this.knowledgeMapper = knowledgeMapper;
    }

    @Override
    public List<Map<String, Object>> list() {
        QueryWrapper<PetKnowledge> qw = new QueryWrapper<>();
        qw.orderByAsc("id");
        List<Map<String, Object>> out = new ArrayList<>();
        for (PetKnowledge k : knowledgeMapper.selectList(qw)) out.add(view(k));
        return out;
    }

    @Override
    @Transactional
    public Map<String, Object> read(Long id) {
        PetKnowledge k = knowledgeMapper.selectById(id);
        if (k == null) return null;
        k.setViews((k.getViews() == null ? 0 : k.getViews()) + 1);
        k.setUpdatedAt(LocalDateTime.now());
        knowledgeMapper.updateById(k);
        return view(k);
    }

    @Override
    public Map<String, Object> create(Map<String, Object> body) {
        PetKnowledge k = new PetKnowledge();
        applyFields(k, body, true);
        LocalDateTime now = LocalDateTime.now();
        k.setCreatedAt(now);
        k.setUpdatedAt(now);
        k.setViews(0L);
        knowledgeMapper.insert(k);
        return view(k);
    }

    @Override
    public Map<String, Object> update(Long id, Map<String, Object> patch) {
        PetKnowledge k = knowledgeMapper.selectById(id);
        if (k == null) throw new AuthException("notfound", "文章不存在", 404);
        applyFields(k, patch, false);
        k.setUpdatedAt(LocalDateTime.now());
        knowledgeMapper.updateById(k);
        return view(k);
    }

    @Override
    public void delete(Long id) {
        if (knowledgeMapper.selectById(id) == null) {
            throw new AuthException("notfound", "文章不存在", 404);
        }
        knowledgeMapper.deleteById(id);
    }

    /* ---------------- 内部辅助 ---------------- */

    /** 按 body 填充实体。required=true 时强制校验标题与分类 */
    private void applyFields(PetKnowledge k, Map<String, Object> body, boolean required) {
        String title = str(body, "title");
        if (title != null) {
            if (title.length() > 80) throw new AuthException("title", "标题过长（最多 80 字）");
            k.setTitle(title);
        } else if (required) {
            throw new AuthException("title", "请填写文章标题");
        }

        String cat = str(body, "cat");
        if (cat != null) {
            k.setCat(cat);
        } else if (required) {
            throw new AuthException("cat", "请选择文章分类");
        }

        String summary = str(body, "summary");
        if (summary != null) k.setSummary(summary);

        // 标签：接受数组或「逗号/顿号分隔」字符串，统一转 JSON 数组
        Object tags = body == null ? null : body.get("tags");
        List<String> tagList = normalizeTags(tags);
        if (tagList != null) k.setTags(toJson(tagList));

        // 正文：sections 数组 [{h,p}]，剔除空小节后存 JSON
        Object sections = body == null ? null : body.get("sections");
        List<Map<String, String>> sectionList = normalizeSections(sections);
        if (sectionList != null) k.setContent(toJson(sectionList));
    }

    /** 输出结构：{ id, cat, title, summary, tags:[], sections:[{h,p}], views, createdAt } */
    private Map<String, Object> view(PetKnowledge k) {
        Map<String, Object> v = new LinkedHashMap<>();
        v.put("id", k.getId());
        v.put("cat", k.getCat());
        v.put("title", k.getTitle());
        v.put("summary", k.getSummary());
        v.put("tags", parseJsonList(k.getTags()));
        v.put("sections", parseJsonList(k.getContent()));
        v.put("views", k.getViews() == null ? 0 : k.getViews());
        v.put("createdAt", k.getCreatedAt());
        v.put("updatedAt", k.getUpdatedAt() == null ? k.getCreatedAt() : k.getUpdatedAt());
        return v;
    }

    /** 标签归一化：["a","b"] / "a,b、c" -> List；null 表示调用方没传该字段 */
    @SuppressWarnings("unchecked")
    private List<String> normalizeTags(Object tags) {
        if (tags == null) return null;
        List<String> out = new ArrayList<>();
        if (tags instanceof List<?> list) {
            for (Object t : list) {
                String s = t == null ? "" : String.valueOf(t).trim();
                if (!s.isEmpty()) out.add(s);
            }
            return out;
        }
        String s = String.valueOf(tags).trim();
        if (s.isEmpty()) return out;
        for (String part : s.split("[,，、]")) {
            String t = part.trim();
            if (!t.isEmpty()) out.add(t);
        }
        return out;
    }

    /** 小节归一化：[{h,p}]，剔除 h 与 p 全空的行；null 表示调用方没传该字段 */
    @SuppressWarnings("unchecked")
    private List<Map<String, String>> normalizeSections(Object sections) {
        if (sections == null) return null;
        List<Map<String, String>> out = new ArrayList<>();
        if (sections instanceof List<?> list) {
            for (Object o : list) {
                if (!(o instanceof Map<?, ?> m)) continue;
                String h = String.valueOf(m.get("h") == null ? "" : m.get("h")).trim();
                String p = String.valueOf(m.get("p") == null ? "" : m.get("p")).trim();
                if (h.isEmpty() && p.isEmpty()) continue;
                Map<String, String> row = new LinkedHashMap<>();
                row.put("h", h);
                row.put("p", p);
                out.add(row);
            }
        }
        return out;
    }

    private String toJson(Object o) {
        try {
            return JSON.writeValueAsString(o);
        } catch (Exception e) {
            throw new AuthException("content", "内容序列化失败");
        }
    }

    /** JSON 数组 -> List；解析失败给空列表，页面不至于崩 */
    private List<Object> parseJsonList(String raw) {
        if (raw == null || raw.isBlank()) return new ArrayList<>();
        try {
            return JSON.readValue(raw, List.class);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private static String str(Map<String, Object> m, String key) {
        Object v = m == null ? null : m.get(key);
        if (v == null) return null;
        String s = String.valueOf(v).trim();
        return s.isEmpty() ? null : s;
    }
}
