package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 用户养成资源（表 pet_user_resource）：养分 / 食物。每个用户一行，主键为用户名。
 */
@TableName("pet_user_resource")
public class PetUserResource {

    /** 用户标识（用户名），业务主键 */
    @TableId(value = "user_key", type = IdType.INPUT)
    private String userKey;

    /** 养分 */
    private Integer nutrients;

    /** 食物 */
    private Integer food;

    private Long updatedAt;

    public String getUserKey() { return userKey; }
    public void setUserKey(String userKey) { this.userKey = userKey; }

    public Integer getNutrients() { return nutrients; }
    public void setNutrients(Integer nutrients) { this.nutrients = nutrients; }

    public Integer getFood() { return food; }
    public void setFood(Integer food) { this.food = food; }

    public Long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Long updatedAt) { this.updatedAt = updatedAt; }
}
