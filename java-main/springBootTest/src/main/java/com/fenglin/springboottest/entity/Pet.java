package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 用户领养的宠物（表 pet）。status: egg=蛋, hatched=已孵化。
 */
@TableName("pet")
public class Pet {

    /** 宠物 id（业务生成） */
    @TableId(type = IdType.INPUT)
    private String id;

    /** 所属用户 */
    private String userKey;

    /** 种类 id（对应 pet_species.id） */
    private String speciesId;

    /** 宠物昵称 */
    private String name;

    /** egg / hatched */
    private String status;

    /** 亲密度 0~100 */
    private Integer intimacy;

    /** 饱食度 0~100 */
    private Integer satiety;

    /** 心情 0~100 */
    private Integer mood;

    private Long createdAt;
    private Long hatchedAt;
    private Long lastFed;
    private Long lastInteract;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserKey() { return userKey; }
    public void setUserKey(String userKey) { this.userKey = userKey; }

    public String getSpeciesId() { return speciesId; }
    public void setSpeciesId(String speciesId) { this.speciesId = speciesId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getIntimacy() { return intimacy; }
    public void setIntimacy(Integer intimacy) { this.intimacy = intimacy; }

    public Integer getSatiety() { return satiety; }
    public void setSatiety(Integer satiety) { this.satiety = satiety; }

    public Integer getMood() { return mood; }
    public void setMood(Integer mood) { this.mood = mood; }

    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }

    public Long getHatchedAt() { return hatchedAt; }
    public void setHatchedAt(Long hatchedAt) { this.hatchedAt = hatchedAt; }

    public Long getLastFed() { return lastFed; }
    public void setLastFed(Long lastFed) { this.lastFed = lastFed; }

    public Long getLastInteract() { return lastInteract; }
    public void setLastInteract(Long lastInteract) { this.lastInteract = lastInteract; }
}
