package com.fenglin.springboottest.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 「发现」帖子业务接口。
 *
 * 视图结构（与前端 login-vue/src/api/post.js 头部注释的约定一致）：
 * {
 *   id, title, content, category, tags: [String],
 *   author, authorName, createdAt: 毫秒时间戳,
 *   media: [{ type: 'image'|'video', src, name, size }],
 *   likes: [userKey],                       // 一人一赞，数组即点赞人列表
 *   comments: [{ id, author, authorName, content, createdAt }]
 * }
 *
 * 说明：当前没有登录会话/JWT，author 由前端透传（演示级实现）；
 * 生产环境必须改为从登录态取当前用户，且删除/评论要做归属与权限校验。
 */
public interface PostService {

    /** 帖子列表（按发布时间倒序）；category 为空返回全部 */
    List<Map<String, Object>> list(String category, String userKey);

    /** 帖子详情；不存在返回 null */
    Map<String, Object> detail(Long id, String userKey);

    /** 发帖：校验字段与媒体，保存文件到磁盘并落库，返回新帖视图 */
    Map<String, Object> create(String title, String content, String category, String tags,
                               String author, String authorName, List<MultipartFile> files);

    /** 删除帖子（连同媒体记录与磁盘文件、点赞、评论）；author 与帖主不符时抛 403 */
    void delete(Long id, String author);

    /** 点赞 / 取消点赞（切换），返回更新后的帖子视图 */
    Map<String, Object> toggleLike(Long id, String userKey);

    /** 发表评论，返回更新后的帖子视图 */
    Map<String, Object> addComment(Long id, String author, String authorName, String content);
}
