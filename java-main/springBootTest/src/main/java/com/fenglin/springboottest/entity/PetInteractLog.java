package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 每日互动（摸摸）次数（表 pet_interact_log）。每日上限 5 次。
 */
@TableName("pet_interact_log")
public class PetInteractLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String userKey;
    private String interactDate;
    private Integer interactCount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserKey() { return userKey; }
    public void setUserKey(String userKey) { this.userKey = userKey; }

    public String getInteractDate() { return interactDate; }
    public void setInteractDate(String interactDate) { this.interactDate = interactDate; }

    public Integer getInteractCount() { return interactCount; }
    public void setInteractCount(Integer interactCount) { this.interactCount = interactCount; }
}
