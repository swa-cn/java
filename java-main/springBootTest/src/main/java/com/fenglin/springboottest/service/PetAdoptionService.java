package com.fenglin.springboottest.service;

import java.util.Map;

/**
 * 萌宠领养养成业务接口（对应前端 login-vue 的领养模块）。
 *
 * 所有写操作在执行后都会返回最新的「面板数据」（dashboard），
 * 便于前端一次刷新；读取用 dashboard 即可拿到全部状态。
 */
public interface PetAdoptionService {

    /** 聚合面板数据：物种、资源、宠物、任务、互动、赠送、好友 */
    Map<String, Object> dashboard(String owner);

    /** 领养一枚蛋 */
    Map<String, Object> adopt(String owner, String speciesId, String name);

    /** 孵化（消耗养分） */
    Map<String, Object> hatch(String owner, String petId);

    /** 喂食（消耗食物，加亲密度） */
    Map<String, Object> feed(String owner, String petId);

    /** 摸摸互动（每日上限 5，加亲密度） */
    Map<String, Object> interact(String owner, String petId);

    /** 标记任务完成（先完成才能领取） */
    Map<String, Object> completeTask(String owner, String taskId);

    /** 领取任务奖励（必须已完成） */
    Map<String, Object> claimTask(String owner, String taskId);

    /** 给好友赠送（校验好友关系，每日上限 3，同好友每日 1 次） */
    Map<String, Object> sendGift(String owner, String friendKey);

    /** 领取好友赠送（按礼物 id 领取 pending 的赠送，每日上限 3） */
    Map<String, Object> claimGift(String owner, Long giftId);

    /** 给宠物改名 */
    Map<String, Object> rename(String owner, String petId, String name);
}
