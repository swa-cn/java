package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 帖子媒体（图片/视频）（表 pet_post_media）。
 */
@TableName("pet_post_media")
public class PetPostMedia {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属帖子 id */
    private Long postId;

    /** image / video */
    private String mediaType;

    /** 访问地址（/uploads/...） */
    private String url;

    /** 原始文件名 */
    private String name;

    /** 文件体积 */
    private Long sizeBytes;

    /** 排序号 */
    private Integer sortNo;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }

    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(Long sizeBytes) { this.sizeBytes = sizeBytes; }

    public Integer getSortNo() { return sortNo; }
    public void setSortNo(Integer sortNo) { this.sortNo = sortNo; }

}