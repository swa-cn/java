package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 成就奖励领取记录（表 pet_achievement_claim）。
 * 每个用户对每个成就只能领取一次，靠 (user_key, achievement_id) 唯一索引保证。
 */
@TableName("pet_achievement_claim")
public class PetAchievementClaim {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String userKey;
    private String achievementId;
    private Long claimedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserKey() { return userKey; }
    public void setUserKey(String userKey) { this.userKey = userKey; }

    public String getAchievementId() { return achievementId; }
    public void setAchievementId(String achievementId) { this.achievementId = achievementId; }

    public Long getClaimedAt() { return claimedAt; }
    public void setClaimedAt(Long claimedAt) { this.claimedAt = claimedAt; }
}
