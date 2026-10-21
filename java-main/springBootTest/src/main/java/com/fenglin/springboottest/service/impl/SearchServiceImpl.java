package com.fenglin.springboottest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fenglin.springboottest.entity.AppUser;
import com.fenglin.springboottest.entity.PetBreed;
import com.fenglin.springboottest.entity.PetCategory;
import com.fenglin.springboottest.entity.PetPost;
import com.fenglin.springboottest.entity.PetPostMedia;
import com.fenglin.springboottest.mapper.AppUserMapper;
import com.fenglin.springboottest.mapper.PetBreedMapper;
import com.fenglin.springboottest.mapper.PetCategoryMapper;
import com.fenglin.springboottest.mapper.PetPostMapper;
import com.fenglin.springboottest.mapper.PetPostMediaMapper;
import com.fenglin.springboottest.service.SearchService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 全局搜索实现。
 *
 * 设计取舍：
 * 1. 分类 / 品种是极少量配置数据（几个分类、二十来个品种），一次性读进内存过滤，
 *    既避免为每种匹配组合拼 OR 条件，也让「分类名命中 → 该分类下所有品种都相关」这类
 *    跨表规则写起来更直观；表变大时再下推到 SQL。
 * 2. 帖子 / 用户数据量不可控，用 SQL LIKE 过滤 + LIMIT，只在库内取需要的行。
 * 3. 结果按相关度排序：名称完全相等 > 前缀命中 > 包含命中 > 简介/正文命中。
 *    没有相关度差异时保持原有顺序（分类/品种按 sort_no，帖子按时间倒序）。
 */
@Service
public class SearchServiceImpl implements SearchService {

    /** 单组默认条数：够铺满结果页首屏，又不至于把响应撑大 */
    private static final int DEFAULT_LIMIT = 20;

    /** 摘要截断长度（搜索结果卡片只放一句话） */
    private static final int SUMMARY_MAX = 46;

    private final PetCategoryMapper categoryMapper;
    private final PetBreedMapper breedMapper;
    private final PetPostMapper postMapper;
    private final PetPostMediaMapper mediaMapper;
    private final AppUserMapper userMapper;

    public SearchServiceImpl(PetCategoryMapper categoryMapper,
                             PetBreedMapper breedMapper,
                             PetPostMapper postMapper,
                             PetPostMediaMapper mediaMapper,
                             AppUserMapper userMapper) {
        this.categoryMapper = categoryMapper;
        this.breedMapper = breedMapper;
        this.postMapper = postMapper;
        this.mediaMapper = mediaMapper;
        this.userMapper = userMapper;
    }

    @Override
    public Map<String, Object> search(String keyword, int limitPerGroup) {
        String q = keyword == null ? "" : keyword.trim();
        int limit = limitPerGroup > 0 ? Math.min(limitPerGroup, 50) : DEFAULT_LIMIT;

        if (q.isEmpty()) {
            return result(q, Collections.emptyList(), Collections.emptyList(),
                    Collections.emptyList(), Collections.emptyList());
        }

        // ---------- 分类 + 品种（内存过滤） ----------
        List<PetCategory> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<PetCategory>()
                        .orderByAsc(PetCategory::getSortNo)
                        .orderByAsc(PetCategory::getId));
        List<PetBreed> breeds = breedMapper.selectList(
                new LambdaQueryWrapper<PetBreed>()
                        .orderByAsc(PetBreed::getSortNo)
                        .orderByAsc(PetBreed::getId));

        Map<String, PetCategory> categoryById = new LinkedHashMap<>();
        for (PetCategory c : categories) {
            categoryById.put(c.getId(), c);
        }

        // 分类名命中的分类 id：用于把该分类下的全部品种也纳入结果
        Set<String> hitCategoryIds = new LinkedHashSet<>();
        List<Scored<Map<String, Object>>> categoryHits = new ArrayList<>();
        for (PetCategory c : categories) {
            int score = nameScore(c.getName(), q);
            if (score > 0) {
                hitCategoryIds.add(c.getId());
                Map<String, Object> v = new LinkedHashMap<>();
                v.put("id", c.getId());
                v.put("name", c.getName());
                v.put("icon", c.getIcon());
                v.put("color", c.getColor());
                v.put("image", c.getImage());
                v.put("breedCount", (int) breeds.stream()
                        .filter(b -> c.getId().equals(b.getCategoryId())).count());
                categoryHits.add(new Scored<>(score, v));
            }
        }

        List<Scored<Map<String, Object>>> breedHits = new ArrayList<>();
        for (PetBreed b : breeds) {
            PetCategory parent = categoryById.get(b.getCategoryId());
            String categoryName = parent != null ? parent.getName() : "";

            int score = nameScore(b.getName(), q);
            if (score == 0 && containsIgnoreCase(b.getDescription(), q)) {
                score = 2; // 简介命中：最弱相关
            }
            if (score == 0 && containsIgnoreCase(categoryName, q)) {
                score = 3; // 分类名命中：该分类下品种都算相关
            }
            if (score == 0) {
                continue;
            }
            Map<String, Object> v = new LinkedHashMap<>();
            v.put("name", b.getName());
            v.put("image", b.getImage());
            v.put("desc", b.getDescription());
            v.put("categoryId", b.getCategoryId());
            v.put("categoryName", categoryName);
            v.put("categoryColor", parent != null ? parent.getColor() : "#ff6fa8");
            v.put("categoryIcon", parent != null ? parent.getIcon() : "萌");
            breedHits.add(new Scored<>(score, v));
        }

        // ---------- 帖子（SQL LIKE） ----------
        LambdaQueryWrapper<PetPost> postWrapper = new LambdaQueryWrapper<>();
        postWrapper.and(w -> w.like(PetPost::getTitle, q)
                        .or().like(PetPost::getContent, q)
                        .or().like(PetPost::getTags, q))
                .orderByDesc(PetPost::getCreatedAt)
                .orderByDesc(PetPost::getId)
                .last("LIMIT " + (limit * 2)); // 多取一些，排序后截断
        List<PetPost> postRows = new ArrayList<>(postMapper.selectList(postWrapper));

        // 分类命中的帖子也算相关（例如搜「猫」时分类=cat 的动态）
        if (!hitCategoryIds.isEmpty() && postRows.size() < limit * 2) {
            LambdaQueryWrapper<PetPost> byCategory = new LambdaQueryWrapper<>();
            byCategory.in(PetPost::getCategory, hitCategoryIds)
                    .orderByDesc(PetPost::getCreatedAt)
                    .orderByDesc(PetPost::getId)
                    .last("LIMIT " + (limit * 2));
            Set<Long> seen = new HashSet<>();
            for (PetPost p : postRows) {
                seen.add(p.getId());
            }
            for (PetPost p : postMapper.selectList(byCategory)) {
                if (seen.add(p.getId())) {
                    postRows.add(p);
                }
            }
        }

        Map<Long, String> coverByPostId = firstImageByPost(postRows);
        List<Scored<Map<String, Object>>> postHits = new ArrayList<>();
        for (PetPost p : postRows) {
            PetCategory parent = p.getCategory() == null ? null : categoryById.get(p.getCategory());
            String categoryName = parent != null ? parent.getName() : "";

            int score = nameScore(p.getTitle(), q);
            if (score == 0 && containsIgnoreCase(p.getTags(), q)) {
                score = 4; // 标签命中，比正文精准
            }
            if (score == 0 && containsIgnoreCase(p.getContent(), q)) {
                score = 3;
            }
            if (score == 0 && containsIgnoreCase(categoryName, q)) {
                score = 2;
            }
            if (score == 0) {
                continue;
            }
            Map<String, Object> v = new LinkedHashMap<>();
            v.put("id", String.valueOf(p.getId()));
            v.put("postId", String.valueOf(p.getId()));
            v.put("title", isBlank(p.getTitle()) ? autoTitle(p.getContent()) : p.getTitle());
            v.put("summary", summary(p.getContent()));
            v.put("cover", categoryName.isEmpty() ? "萌" : categoryName.substring(0, 1));
            v.put("image", coverByPostId.getOrDefault(p.getId(), ""));
            v.put("category", p.getCategory());
            v.put("categoryName", categoryName);
            v.put("author", p.getAuthor());
            v.put("authorName", isBlank(p.getAuthorName()) ? p.getAuthor() : p.getAuthorName());
            v.put("createdAt", toMillis(p.getCreatedAt()));
            postHits.add(new Scored<>(score, v));
        }

        // ---------- 用户（SQL LIKE，排除空账号） ----------
        LambdaQueryWrapper<AppUser> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.isNotNull(AppUser::getUsername)
                .and(w -> w.like(AppUser::getUsername, q).or().like(AppUser::getNickname, q))
                .orderByAsc(AppUser::getId)
                .last("LIMIT " + limit);
        List<AppUser> userRows = userMapper.selectList(userWrapper);
        List<Scored<Map<String, Object>>> userHits = new ArrayList<>();
        for (AppUser u : userRows) {
            int score = Math.max(nameScore(u.getNickname(), q), nameScore(u.getUsername(), q));
            if (score == 0) {
                continue;
            }
            Map<String, Object> v = new LinkedHashMap<>();
            v.put("key", u.getUsername());
            v.put("name", isBlank(u.getNickname()) ? u.getUsername() : u.getNickname());
            v.put("username", u.getUsername());
            userHits.add(new Scored<>(score, v));
        }

        return result(q,
                top(categoryHits, limit),
                top(breedHits, limit),
                top(postHits, limit),
                top(userHits, limit));
    }

    /* ================= 组装 ================= */

    private static Map<String, Object> result(String q,
                                              List<Map<String, Object>> categories,
                                              List<Map<String, Object>> breeds,
                                              List<Map<String, Object>> posts,
                                              List<Map<String, Object>> users) {
        Map<String, Object> counts = new LinkedHashMap<>();
        counts.put("categories", categories.size());
        counts.put("breeds", breeds.size());
        counts.put("posts", posts.size());
        counts.put("users", users.size());

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        m.put("query", q);
        m.put("total", categories.size() + breeds.size() + posts.size() + users.size());
        m.put("counts", counts);
        m.put("categories", categories);
        m.put("breeds", breeds);
        m.put("posts", posts);
        m.put("users", users);
        return m;
    }

    /** 按 score 降序取前 limit 条（Collections.sort 稳定，同级保持原有顺序） */
    private static List<Map<String, Object>> top(List<Scored<Map<String, Object>>> hits, int limit) {
        hits.sort((a, b) -> Integer.compare(b.score, a.score));
        List<Map<String, Object>> out = new ArrayList<>(Math.min(hits.size(), limit));
        for (int i = 0; i < hits.size() && i < limit; i++) {
            out.add(hits.get(i).value);
        }
        return out;
    }

    /** 一次查出这批帖子的首图，避免逐帖查（N+1） */
    private Map<Long, String> firstImageByPost(List<PetPost> posts) {
        Map<Long, String> map = new LinkedHashMap<>();
        if (posts.isEmpty()) {
            return map;
        }
        List<Long> ids = new ArrayList<>(posts.size());
        for (PetPost p : posts) {
            ids.add(p.getId());
        }
        List<PetPostMedia> media = mediaMapper.selectList(
                new LambdaQueryWrapper<PetPostMedia>()
                        .in(PetPostMedia::getPostId, ids)
                        .orderByAsc(PetPostMedia::getSortNo)
                        .orderByAsc(PetPostMedia::getId));
        for (PetPostMedia md : media) {
            if (md.getPostId() == null || isBlank(md.getUrl())) {
                continue;
            }
            boolean isImage = md.getMediaType() == null || "image".equalsIgnoreCase(md.getMediaType());
            if (isImage) {
                map.putIfAbsent(md.getPostId(), md.getUrl());
            }
        }
        return map;
    }

    /* ================= 文本工具 ================= */

    /**
     * 名称相关度：完全相等 10 / 前缀 8 / 包含 6 / 不命中 0。
     * 中文没有大小写问题，但英文品种名（如 Corgi）需要忽略大小写。
     */
    private static int nameScore(String name, String q) {
        if (isBlank(name)) {
            return 0;
        }
        String n = name.trim().toLowerCase();
        String k = q.toLowerCase();
        if (n.equals(k)) {
            return 10;
        }
        if (n.startsWith(k)) {
            return 8;
        }
        return n.contains(k) ? 6 : 0;
    }

    private static boolean containsIgnoreCase(String text, String q) {
        return text != null && text.toLowerCase().contains(q.toLowerCase());
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /** 无标题时用正文开头当标题 */
    private static String autoTitle(String content) {
        String s = summary(content);
        return s.isEmpty() ? "萌宠动态" : s;
    }

    /** 正文压成一行并截断，避免把整段正文塞进搜索结果卡片 */
    private static String summary(String content) {
        if (isBlank(content)) {
            return "";
        }
        String s = content.replaceAll("\\s+", " ").trim();
        return s.length() <= SUMMARY_MAX ? s : s.substring(0, SUMMARY_MAX) + "…";
    }

    /** LocalDateTime -> 毫秒时间戳（与 PostService 的视图字段保持一致） */
    private static long toMillis(LocalDateTime t) {
        return t == null ? 0L : t.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /** 值 + 相关度的临时载体（只在本类内部使用） */
    private static final class Scored<T> {
        final int score;
        final T value;

        Scored(int score, T value) {
            this.score = score;
            this.value = value;
        }
    }
}
