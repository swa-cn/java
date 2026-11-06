package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 好友赠送记录（表 pet_gift_log）。同好友每日 1 次，每日上限 3 次。
 */
@TableName("pet_gift_log")
public class PetGiftLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String userKey;
    private String friendKey;
    private String giftDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserKey() { return userKey; }
    public void setUserKey(String userKey) { this.userKey = userKey; }

    public String getFriendKey() { return friendKey; }
    public void setFriendKey(String friendKey) { this.friendKey = friendKey; }

    public String getGiftDate() { return giftDate; }
    public void setGiftDate(String giftDate) { this.giftDate = giftDate; }
}
