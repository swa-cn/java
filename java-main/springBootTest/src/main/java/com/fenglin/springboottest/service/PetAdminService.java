package com.fenglin.springboottest.service;

import com.fenglin.springboottest.entity.PetBreed;
import com.fenglin.springboottest.entity.PetCategory;

import java.util.List;
import java.util.Map;

/**
 * 后台管理：萌宠分类 / 品种图鉴的写操作（增删改 + 管理视角的全字段查询）。
 *
 * 与 PetCategoryService（只读、面向首页展示）的分工：
 *  - PetCategoryService：组装 { id, name, icon, color, image, breeds:[{name,image,desc}] } 视图；
 *  - 本服务：面向后台表格，返回完整字段（含 sortNo），并提供新增/修改/删除。
 */
public interface PetAdminService {

    /** 分类列表（管理视角，含 sortNo 与品种数） */
    List<Map<String, Object>> listCategories();

    /** 新增分类（id 为业务主键，如 hamster），已存在则报 409 */
    PetCategory createCategory(PetCategory c);

    /** 修改分类属性（名称/图标/颜色/封面/排序），id 不可改 */
    PetCategory updateCategory(String id, PetCategory patch);

    /** 删除分类：连带删除其下全部品种 */
    void deleteCategory(String id);

    /** 指定分类下的品种列表（管理视角，含 sortNo 与 description） */
    List<Map<String, Object>> listBreeds(String categoryId);

    /** 新增品种，category_id 必须存在 */
    PetBreed createBreed(PetBreed b);

    /** 修改品种属性（名称/图片/简介/所属分类/排序） */
    PetBreed updateBreed(Long id, PetBreed patch);

    /** 删除品种 */
    void deleteBreed(Long id);
}
