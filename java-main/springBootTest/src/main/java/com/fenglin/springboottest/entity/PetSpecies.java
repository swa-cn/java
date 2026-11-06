package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 宠物种类目录（表 pet_species）。主键为业务 id（cat/dog/...），由种子数据固定写入。
 */
@TableName("pet_species")
public class PetSpecies {

    /** 种类 id：cat/dog/rabbit/hamster/bird/fish（业务主键） */
    @TableId(type = IdType.INPUT)
    private String id;

    private String name;
    private String emoji;
    private String color;
    private String description;
    private Integer hatchCost;
    private Integer sortOrder;
    private Long createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getHatchCost() { return hatchCost; }
    public void setHatchCost(Integer hatchCost) { this.hatchCost = hatchCost; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
}
