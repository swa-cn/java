package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 好友私信（表 pet_message）。
 *
 * 每条记录是一句话，按「会话」= (owner_key, peer_key) 聚合。
 * owner_key 是消息归属方，from_key 是实际发送者：
 *   from_key == owner_key 表示这条是「我发的」（显示在右侧）；
 *   否则是「对方发的」（显示在左侧，也是未读统计依据）。
 */
@TableName("pet_message")
public class PetMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会话归属方登录名 */
    private String ownerKey;

    /** 会话对方登录名 */
    private String peerKey;

    /** 对方昵称（展示用） */
    private String peerName;

    /** 实际发送者登录名 */
    private String fromKey;

    /** 发送者昵称 */
    private String fromName;

    /** 消息正文 */
    private String content;

    /** 是否已读（仅对方发来的消息会被标记） */
    private Boolean readFlag;

    /** 发送时间 */
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOwnerKey() { return ownerKey; }
    public void setOwnerKey(String ownerKey) { this.ownerKey = ownerKey; }

    public String getPeerKey() { return peerKey; }
    public void setPeerKey(String peerKey) { this.peerKey = peerKey; }

    public String getPeerName() { return peerName; }
    public void setPeerName(String peerName) { this.peerName = peerName; }

    public String getFromKey() { return fromKey; }
    public void setFromKey(String fromKey) { this.fromKey = fromKey; }

    public String getFromName() { return fromName; }
    public void setFromName(String fromName) { this.fromName = fromName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Boolean getReadFlag() { return readFlag; }
    public void setReadFlag(Boolean readFlag) { this.readFlag = readFlag; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
