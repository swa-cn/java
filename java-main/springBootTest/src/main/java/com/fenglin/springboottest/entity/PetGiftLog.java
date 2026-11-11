package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 好友赠送记录（表 pet_gift_log）。
 *
 * 语义：一行 = 「某用户给某个好友赠送了一份礼物」，状态为 pending / claimed。
 *   user_key   = 赠送方（sender）
 *   friend_key = 领取方（recipient）
 *   status     = 'pending'（待领取）| 'claimed'（已领取）
 * 同一好友每天只能赠送 1 次（唯一索引约束），赠送方每日上限 3 次、领取方每日上限 3 次。
 */
@TableName("pet_gift_log")
public class PetGiftLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String userKey;
    private String friendKey;
    private String giftDate;
    private String status;
    private Long claimedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserKey() { return userKey; }
    public void setUserKey(String userKey) { this.userKey = userKey; }

    public String getFriendKey() { return friendKey; }
    public void setFriendKey(String friendKey) { this.friendKey = friendKey; }

    public String getGiftDate() { return giftDate; }
    public void setGiftDate(String giftDate) { this.giftDate = giftDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getClaimedAt() { return claimedAt; }
    public void setClaimedAt(Long claimedAt) { this.claimedAt = claimedAt; }
}
