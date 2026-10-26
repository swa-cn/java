package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 萌宠品种图鉴（表 pet_breed）。
 *
 * 字段名 description 与前端 petData.js 的 desc 对应，
 * 组装视图时再映射为 desc（见 PetCategoryServiceImpl）。
 */
@TableName("pet_breed")
public class PetBreed {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属分类 id */
    private String categoryId;

    /** 品种名 */
    private String name;

    /** 品种图 */
    private String image;

    /** 品种简介（前端字段名为 desc） */
    private String description;

    /** 排序（升序） */
    private Integer sortNo;

    /** 性格（首页品种详情展示） */
    private String temperament;

    /** 饲养难度：低/中/高 */
    private String careLevel;

    /** 适合人群 */
    private String suitableFor;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getSortNo() { return sortNo; }
    public void setSortNo(Integer sortNo) { this.sortNo = sortNo; }

    public String getTemperament() { return temperament; }
    public void setTemperament(String temperament) { this.temperament = temperament; }

    public String getCareLevel() { return careLevel; }
    public void setCareLevel(String careLevel) { this.careLevel = careLevel; }

    public String getSuitableFor() { return suitableFor; }
    public void setSuitableFor(String suitableFor) { this.suitableFor = suitableFor; }
}
