package com.fenglin.springboottest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fenglin.springboottest.entity.AppUser;
import com.fenglin.springboottest.entity.PetFollow;
import com.fenglin.springboottest.mapper.AppUserMapper;
import com.fenglin.springboottest.mapper.PetFollowMapper;
import com.fenglin.springboottest.service.FollowService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 关注 / 粉丝实现（表 pet_follow，单向关注，同一对唯一索引兜底）。
 */
@Service
public class FollowServiceImpl implements FollowService {

    private final PetFollowMapper followMapper;
    private final AppUserMapper userMapper;

    public FollowServiceImpl(PetFollowMapper followMapper, AppUserMapper userMapper) {
        this.followMapper = followMapper;
        this.userMapper = userMapper;
    }

    @Override
    public Map<String, Object> toggle(String follower, String followee) {
        Map<String, Object> m = new LinkedHashMap<>();
        if (follower == null || followee == null || follower.equalsIgnoreCase(followee)) {
            m.put("ok", false);
            m.put("reason", follower != null && follower.equalsIgnoreCase(followee) ? "self" : "unknown");
            return m;
        }
        // 目标用户必须存在
        AppUser target = findByKey(followee);
        if (target == null) {
            m.put("ok", false);
            m.put("reason", "notfound");
            return m;
        }
        PetFollow exist = followMapper.selectOne(new LambdaQueryWrapper<PetFollow>()
                .eq(PetFollow::getFollowerKey, follower)
                .eq(PetFollow::getFolloweeKey, followee));
        boolean following;
        if (exist != null) {
            followMapper.deleteById(exist.getId());
            following = false;
        } else {
            PetFollow f = new PetFollow();
            f.setFollowerKey(follower);
            f.setFolloweeKey(followee);
            f.setCreatedAt(LocalDateTime.now());
            followMapper.insert(f);
            following = true;
        }
        m.put("ok", true);
        m.put("following", following);
        m.put("followers", followerCount(followee));
        m.put("following_count", followingCount(followee));
        return m;
    }

    @Override
    public boolean isFollowing(String follower, String followee) {
        if (follower == null || followee == null) return false;
        return followMapper.selectCount(new LambdaQueryWrapper<PetFollow>()
                .eq(PetFollow::getFollowerKey, follower)
                .eq(PetFollow::getFolloweeKey, followee)) > 0;
    }

    @Override
    public Map<String, Object> counts(String userKey) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("followers", followerCount(userKey));
        m.put("following_count", followingCount(userKey));
        return m;
    }

    @Override
    public List<Map<String, Object>> following(String userKey) {
        List<PetFollow> rows = followMapper.selectList(new LambdaQueryWrapper<PetFollow>()
                .eq(PetFollow::getFollowerKey, userKey)
                .orderByDesc(PetFollow::getCreatedAt));
        List<Map<String, Object>> out = new ArrayList<>(rows.size());
        for (PetFollow f : rows) out.add(personView(f.getFolloweeKey()));
        return out;
    }

    @Override
    public List<Map<String, Object>> followers(String userKey) {
        List<PetFollow> rows = followMapper.selectList(new LambdaQueryWrapper<PetFollow>()
                .eq(PetFollow::getFolloweeKey, userKey)
                .orderByDesc(PetFollow::getCreatedAt));
        List<Map<String, Object>> out = new ArrayList<>(rows.size());
        for (PetFollow f : rows) out.add(personView(f.getFollowerKey()));
        return out;
    }

    /* ---------------- 内部辅助 ---------------- */

    private long followerCount(String userKey) {
        Long n = followMapper.selectCount(new LambdaQueryWrapper<PetFollow>()
                .eq(PetFollow::getFolloweeKey, userKey));
        return n == null ? 0 : n;
    }

    private long followingCount(String userKey) {
        Long n = followMapper.selectCount(new LambdaQueryWrapper<PetFollow>()
                .eq(PetFollow::getFollowerKey, userKey));
        return n == null ? 0 : n;
    }

    private AppUser findByKey(String username) {
        return userMapper.selectOne(new LambdaQueryWrapper<AppUser>()
                .apply("LOWER(username) = {0}", username.toLowerCase()));
    }

    /** 用户展示视图：昵称优先；用户被删时跳过（key 为空由调用方过滤） */
    private Map<String, Object> personView(String username) {
        Map<String, Object> v = new LinkedHashMap<>();
        v.put("key", username);
        AppUser u = username == null ? null : findByKey(username);
        v.put("name", u != null && u.getNickname() != null && !u.getNickname().isBlank()
                ? u.getNickname() : username);
        return v;
    }
}
