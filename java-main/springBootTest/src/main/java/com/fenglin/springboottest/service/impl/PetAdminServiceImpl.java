package com.fenglin.springboottest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fenglin.springboottest.entity.PetBreed;
import com.fenglin.springboottest.entity.PetCategory;
import com.fenglin.springboottest.exception.AuthException;
import com.fenglin.springboottest.mapper.PetBreedMapper;
import com.fenglin.springboottest.mapper.PetCategoryMapper;
import com.fenglin.springboottest.service.PetAdminService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 后台管理：萌宠分类 / 品种图鉴写操作实现。
 *
 * 校验规则保持轻量（内部后台系统）：
 *  - 分类 id：3-20 位小写字母/数字，新建时必填且唯一；
 *  - 名称：1-40 字符必填；
 *  - 颜色：#RRGGBB（可空，前端有默认值）；
 *  - 排序：整数，空则排到最后（999）。
 */
@Service
public class PetAdminServiceImpl implements PetAdminService {

    private static final Pattern RE_CATEGORY_ID = Pattern.compile("^[a-z0-9]{3,20}$");

    private final PetCategoryMapper categoryMapper;
    private final PetBreedMapper breedMapper;

    public PetAdminServiceImpl(PetCategoryMapper categoryMapper, PetBreedMapper breedMapper) {
        this.categoryMapper = categoryMapper;
        this.breedMapper = breedMapper;
    }

    /* ================= 分类 ================= */

    @Override
    public List<Map<String, Object>> listCategories() {
        List<PetCategory> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<PetCategory>()
                        .orderByAsc(PetCategory::getSortNo)
                        .orderByAsc(PetCategory::getId));

        // 一次取全部品种按分类计数，避免逐分类 count（数据量很小，简单优先）
        List<PetBreed> allBreeds = breedMapper.selectList(null);
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (PetBreed b : allBreeds) {
            counts.merge(b.getCategoryId(), 1, Integer::sum);
        }

        List<Map<String, Object>> out = new ArrayList<>(categories.size());
        for (PetCategory c : categories) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", c.getId());
            m.put("name", c.getName());
            m.put("icon", c.getIcon());
            m.put("color", c.getColor());
            m.put("image", c.getImage());
            m.put("sortNo", c.getSortNo());
            m.put("breedCount", counts.getOrDefault(c.getId(), 0));
            out.add(m);
        }
        return out;
    }

    @Override
    public PetCategory createCategory(PetCategory c) {
        String id = trim(c.getId());
        String name = trim(c.getName());
        if (id == null || !RE_CATEGORY_ID.matcher(id).matches()) {
            throw new AuthException("id", "分类 ID 需为 3-20 位小写字母或数字（如 hamster）");
        }
        if (categoryMapper.selectById(id) != null) {
            throw new AuthException("id", "分类 ID 已存在：" + id, 409);
        }
        if (name == null) {
            throw new AuthException("name", "请填写分类名称");
        }
        c.setId(id);
        c.setName(name);
        c.setIcon(defaultIfBlank(c.getIcon(), "萌"));
        c.setColor(defaultIfBlank(c.getColor(), "#ff6fa8"));
        c.setSortNo(c.getSortNo() == null ? 999 : c.getSortNo());
        categoryMapper.insert(c);
        return c;
    }

    @Override
    public PetCategory updateCategory(String id, PetCategory patch) {
        PetCategory c = requireCategory(id);
        if (trim(patch.getName()) != null) c.setName(trim(patch.getName()));
        if (patch.getIcon() != null) c.setIcon(defaultIfBlank(patch.getIcon(), "萌"));
        if (patch.getColor() != null) c.setColor(defaultIfBlank(patch.getColor(), c.getColor()));
        if (patch.getImage() != null) c.setImage(trim(patch.getImage()));
        if (patch.getSortNo() != null) c.setSortNo(patch.getSortNo());
        categoryMapper.updateById(c);
        return c;
    }

    @Override
    @Transactional
    public void deleteCategory(String id) {
        requireCategory(id);
        // 先删品种再删分类，避免留下孤儿品种
        breedMapper.delete(new LambdaQueryWrapper<PetBreed>().eq(PetBreed::getCategoryId, id));
        categoryMapper.deleteById(id);
    }

    /* ================= 品种 ================= */

    @Override
    public List<Map<String, Object>> listBreeds(String categoryId) {
        requireCategory(categoryId);
        List<PetBreed> breeds = breedMapper.selectList(
                new LambdaQueryWrapper<PetBreed>()
                        .eq(PetBreed::getCategoryId, categoryId)
                        .orderByAsc(PetBreed::getSortNo)
                        .orderByAsc(PetBreed::getId));
        List<Map<String, Object>> out = new ArrayList<>(breeds.size());
        for (PetBreed b : breeds) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", b.getId());
            m.put("categoryId", b.getCategoryId());
            m.put("name", b.getName());
            m.put("image", b.getImage());
            m.put("description", b.getDescription());
            m.put("temperament", b.getTemperament());
            m.put("careLevel", b.getCareLevel());
            m.put("suitableFor", b.getSuitableFor());
            m.put("sortNo", b.getSortNo());
            out.add(m);
        }
        return out;
    }

    @Override
    public PetBreed createBreed(PetBreed b) {
        String name = trim(b.getName());
        if (name == null) {
            throw new AuthException("name", "请填写品种名称");
        }
        String categoryId = trim(b.getCategoryId());
        if (categoryId == null) {
            throw new AuthException("categoryId", "请选择所属分类");
        }
        requireCategory(categoryId);
        b.setName(name);
        b.setCategoryId(categoryId);
        b.setDescription(trim(b.getDescription()));
        b.setTemperament(trim(b.getTemperament()));
        b.setCareLevel(trim(b.getCareLevel()));
        b.setSuitableFor(trim(b.getSuitableFor()));
        b.setSortNo(b.getSortNo() == null ? 999 : b.getSortNo());
        breedMapper.insert(b);
        return b;
    }

    @Override
    public PetBreed updateBreed(Long id, PetBreed patch) {
        PetBreed b = breedMapper.selectById(id);
        if (b == null) {
            throw new AuthException("notfound", "品种不存在或已被删除", 404);
        }
        if (trim(patch.getName()) != null) b.setName(trim(patch.getName()));
        if (patch.getImage() != null) b.setImage(trim(patch.getImage()));
        if (patch.getDescription() != null) b.setDescription(trim(patch.getDescription()));
        if (patch.getTemperament() != null) b.setTemperament(trim(patch.getTemperament()));
        if (patch.getCareLevel() != null) b.setCareLevel(trim(patch.getCareLevel()));
        if (patch.getSuitableFor() != null) b.setSuitableFor(trim(patch.getSuitableFor()));
        if (patch.getSortNo() != null) b.setSortNo(patch.getSortNo());
        if (patch.getCategoryId() != null && !patch.getCategoryId().isBlank()) {
            // 允许把品种挪到其他分类
            String target = trim(patch.getCategoryId());
            requireCategory(target);
            b.setCategoryId(target);
        }
        breedMapper.updateById(b);
        return b;
    }

    @Override
    public void deleteBreed(Long id) {
        if (breedMapper.deleteById(id) == 0) {
            throw new AuthException("notfound", "品种不存在或已被删除", 404);
        }
    }

    /* ================= 内部辅助 ================= */

    private PetCategory requireCategory(String id) {
        PetCategory c = id == null ? null : categoryMapper.selectById(id.trim());
        if (c == null) {
            throw new AuthException("notfound", "分类不存在：" + id, 404);
        }
        return c;
    }

    private static String trim(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static String defaultIfBlank(String v, String def) {
        return (v == null || v.isBlank()) ? def : v.trim();
    }
}
