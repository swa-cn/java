package com.fenglin.springboottest.service;

import java.util.Map;

/**
 * 关注 / 粉丝业务接口（2026-11 新增，表 pet_follow）。
 */
public interface FollowService {

    /**
     * 关注 / 取消关注（切换）。本人返回 {ok:false, reason:'self'}；
     * 返回 {ok, following(切换后的状态), followers, following_count}
     */
    Map<String, Object> toggle(String follower, String followee);

    /** 是否已关注 */
    boolean isFollowing(String follower, String followee);

    /** 数量：{ followers, following_count } */
    Map<String, Object> counts(String userKey);

    /** 关注列表（我关注的人）：[{ key, name }] */
    java.util.List<Map<String, Object>> following(String userKey);

    /** 粉丝列表（关注我的人）：[{ key, name }] */
    java.util.List<Map<String, Object>> followers(String userKey);
}
