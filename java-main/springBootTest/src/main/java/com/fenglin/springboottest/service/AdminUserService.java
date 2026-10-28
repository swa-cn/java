package com.fenglin.springboottest.service;

import java.util.List;
import java.util.Map;

/**
 * 后台「用户管理」服务（app_user 表）。
 *
 * 权限模型（与需求一一对应）：
 *  - 超级管理员 = 配置项 admin.username 指定的账号（默认 admin），权限最大：
 *    可以创建/提升/降级/删除「管理员」；
 *  - 其他管理员（role=0 但不是 admin 账号）：可以管理普通用户（增删改、重置密码），
 *    但不能把任何人设为管理员，也不能创建管理员。
 *
 * 所有权限校验集中在实现类里，Controller 只负责会话校验与传递 allowAdminRole 标记。
 */
public interface AdminUserService {

    /** 用户列表。keyword 为空返回全部，否则按用户名/昵称模糊匹配。不返回密码。 */
    List<Map<String, Object>> listUsers(String keyword);

    /**
     * 新增用户。body: { username, nickname, password, role }
     * role=0（管理员）时必须 allowAdminRole=true，否则 403。
     */
    Map<String, Object> create(Map<String, Object> body, boolean allowAdminRole);

    /**
     * 修改用户。patch 支持部分字段：{ nickname, password, role }。
     * role 变更必须 allowAdminRole=true；超级管理员账号（admin）的 role 不可改。
     */
    Map<String, Object> update(Long id, Map<String, Object> patch, boolean allowAdminRole);

    /**
     * 删除用户。不能删除当前登录账号自己；admin 账号不可删；
     * 删除管理员账号必须 allowAdminRole=true。
     */
    void delete(Long id, String currentUsername, boolean allowAdminRole);
}
