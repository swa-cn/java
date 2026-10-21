package com.fenglin.springboottest.service;

import java.util.List;
import java.util.Map;

/**
 * 萌宠分类 / 品种图鉴业务接口。
 *
 * 视图结构（与前端 login-vue/src/composables/petData.js 的数据形状一致，
 * 便于前端 petData.js 直接替换数据源）：
 * {
 *   id, name, icon, color, image,
 *   breeds: [{ name, image, desc }]
 * }
 *
 * 说明：分类与品种为低频变更的配置数据，本期只提供只读查询。
 */
public interface PetCategoryService {

    /**
     * 分类列表（含各自的品种），按 sort_no 升序。
     * 一次返回全部，避免前端为每个分类再发一次请求。
     */
    List<Map<String, Object>> listAll();

    /**
     * 单个分类（含品种）；不存在返回 null。
     */
    Map<String, Object> detail(String id);
}
