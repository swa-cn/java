package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 萌宠分类（表 pet_category）。
 *
 * 主键是业务 id（cat/dog/small/bird/fish），与 pet_post.category 对应，
 * 因此不用 AUTO 自增，由种子数据固定写入。
 */
@TableName("pet_category")
public class PetCategory {

    /** 分类 id：cat/dog/small/bird/fish（业务主键） */
    @TableId
    private String id;

    /** 分类名 */
    private String name;

    /** 单字图标 */
    private String icon;

    /** 主题色，如 #ff6fa8 */
    private String color;

    /** 封面图 */
    private String image;

    /** 排序（升序） */
    private Integer sortNo;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public Integer getSortNo() { return sortNo; }
    public void setSortNo(Integer sortNo) { this.sortNo = sortNo; }
}
