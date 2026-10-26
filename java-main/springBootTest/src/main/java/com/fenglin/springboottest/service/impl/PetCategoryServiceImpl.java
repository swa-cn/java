package com.fenglin.springboottest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fenglin.springboottest.entity.PetBreed;
import com.fenglin.springboottest.entity.PetCategory;
import com.fenglin.springboottest.mapper.PetBreedMapper;
import com.fenglin.springboottest.mapper.PetCategoryMapper;
import com.fenglin.springboottest.service.PetCategoryService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 萌宠分类 / 品种图鉴实现。
 *
 * 数据来源：pet_category + pet_breed 两张表，
 * 种子数据见 springBootTest/db/pet_category.sql（也可由管理员直接改库维护）。
 *
 * 为避免 N+1 查询，listAll 一次性取出全部品种再按 category_id 分组，
 * 而不是逐个分类去查品种（分类数量固定且很少，这样最简单也最快）。
 */
@Service
public class PetCategoryServiceImpl implements PetCategoryService {

    private final PetCategoryMapper categoryMapper;
    private final PetBreedMapper breedMapper;

    public PetCategoryServiceImpl(PetCategoryMapper categoryMapper, PetBreedMapper breedMapper) {
        this.categoryMapper = categoryMapper;
        this.breedMapper = breedMapper;
    }

    @Override
    public List<Map<String, Object>> listAll() {
        List<PetCategory> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<PetCategory>()
                        .orderByAsc(PetCategory::getSortNo)
                        .orderByAsc(PetCategory::getId));

        // 一次取出全部品种，按分类分组，避免每个分类查一次
        List<PetBreed> allBreeds = breedMapper.selectList(
                new LambdaQueryWrapper<PetBreed>()
                        .orderByAsc(PetBreed::getSortNo)
                        .orderByAsc(PetBreed::getId));

        Map<String, List<Map<String, Object>>> grouped = new LinkedHashMap<>();
        for (PetBreed b : allBreeds) {
            grouped.computeIfAbsent(b.getCategoryId(), k -> new ArrayList<>()).add(breedView(b));
        }

        List<Map<String, Object>> out = new ArrayList<>(categories.size());
        for (PetCategory c : categories) {
            out.add(categoryView(c, grouped.getOrDefault(c.getId(), new ArrayList<>())));
        }
        return out;
    }

    @Override
    public Map<String, Object> detail(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        PetCategory c = categoryMapper.selectById(id);
        if (c == null) {
            return null;
        }
        List<PetBreed> breeds = breedMapper.selectList(
                new LambdaQueryWrapper<PetBreed>()
                        .eq(PetBreed::getCategoryId, id)
                        .orderByAsc(PetBreed::getSortNo)
                        .orderByAsc(PetBreed::getId));

        List<Map<String, Object>> breedViews = new ArrayList<>(breeds.size());
        for (PetBreed b : breeds) {
            breedViews.add(breedView(b));
        }
        return categoryView(c, breedViews);
    }

    /* ================= 视图组装 ================= */

    private static Map<String, Object> categoryView(PetCategory c, List<Map<String, Object>> breeds) {
        Map<String, Object> v = new LinkedHashMap<>();
        v.put("id", c.getId());
        v.put("name", c.getName());
        v.put("icon", c.getIcon());
        v.put("color", c.getColor());
        v.put("image", c.getImage());
        v.put("breeds", breeds);
        return v;
    }

    /** 字段名对齐前端 petData.js：description -> desc，另带详情页三个资料字段 */
    private static Map<String, Object> breedView(PetBreed b) {
        Map<String, Object> v = new LinkedHashMap<>();
        v.put("name", b.getName());
        v.put("image", b.getImage());
        v.put("desc", b.getDescription());
        v.put("temperament", b.getTemperament());
        v.put("careLevel", b.getCareLevel());
        v.put("suitableFor", b.getSuitableFor());
        return v;
    }
}
