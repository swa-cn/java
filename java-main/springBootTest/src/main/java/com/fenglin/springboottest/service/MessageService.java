package com.fenglin.springboottest.service;

import java.util.List;
import java.util.Map;

/**
 * 好友私信服务。
 */
public interface MessageService {

    /** 会话列表（按最后一条消息倒序） */
    List<Map<String, Object>> conversations(String ownerKey);

    /** 与某人的完整聊天记录 */
    Map<String, Object> conversation(String ownerKey, String peerKey, String peerName);

    /** 发送消息，返回新消息 */
    Map<String, Object> send(String ownerKey, String peerKey, String peerName,
                             String content, String fromName);

    /** 标记会话已读 */
    void markRead(String ownerKey, String peerKey);

    /** 删除会话 */
    void remove(String ownerKey, String peerKey);
}
