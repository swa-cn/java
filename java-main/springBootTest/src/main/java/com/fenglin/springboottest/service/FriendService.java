package com.fenglin.springboottest.service;

import java.util.List;
import java.util.Map;

/**
 * 好友关系服务。
 *
 * 关系模型（关键）：
 *  - 申请阶段：只写一条 pending（申请人 → 对方）。此时对方的好友列表里
 *    会出现一条「待处理申请」（incoming=true），通过后才正式成为好友。
 *  - 成为好友：写双向两条 friend（A→B、B→A），双方列表互相可见。
 */
public interface FriendService {

    /** 某用户的好友列表；status 可传 friend / pending，为空返回全部 */
    List<Map<String, Object>> list(String ownerKey, String status);

    /** 我发出的、等待对方通过的申请（outgoing，可撤回） */
    List<Map<String, Object>> listPending(String ownerKey);

    /** 别人发给我、等待我处理的申请（incoming，可同意/拒绝） */
    List<Map<String, Object>> listIncoming(String ownerKey);

    /** 与某人的关系状态：none / pending_out / pending_in / friend */
    String relation(String ownerKey, String targetKey);

    /** 发送好友申请（已存在关系时幂等返回现有状态） */
    Map<String, Object> add(String ownerKey, String targetKey, String targetName, String source);

    /** 同意申请：真正建立双向好友关系 */
    Map<String, Object> accept(String ownerKey, String targetKey);

    /** 删除好友 / 撤回申请 / 拒绝申请 */
    void remove(String ownerKey, String targetKey);

    /** 按用户名 / 昵称搜索可以添加的用户 */
    List<Map<String, Object>> searchUsers(String ownerKey, String keyword);
}
