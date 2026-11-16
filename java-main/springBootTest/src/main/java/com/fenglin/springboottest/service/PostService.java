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
 * 身份：2026-11 起登录用户名由后端会话（CurrentUser）取得并覆盖前端传参，
 * 前端传的 author/userKey 仅作兼容保留；删除/评论均校验会话身份。
 */
public interface PostService {

    /**
     * 帖子分页列表（按发布时间倒序）；category / author 为空返回全部。
     * 返回 { items: [Post视图], total: 总数 }。
     */
    Map<String, Object> page(String category, String author, String userKey, int page, int size);

    /** 帖子详情；不存在返回 null */
    Map<String, Object> detail(Long id, String userKey);

    /** 发帖：校验字段与媒体，保存文件到磁盘并落库，返回新帖视图 */
    Map<String, Object> create(String title, String content, String category, String tags,
                               String author, String authorName, List<MultipartFile> files);

    /** 删除帖子（连同媒体记录与磁盘文件、点赞、评论）；author 与帖主不符时抛 403 */
    void delete(Long id, String author);

    /** 点赞 / 取消点赞（切换），返回更新后的帖子视图 */
    Map<String, Object> toggleLike(Long id, String userKey);

    /** 发表评论（replyToId 非空 = 楼中楼回复），返回更新后的帖子视图 */
    Map<String, Object> addComment(Long id, String author, String authorName, String content, Long replyToId);

    /** 收藏 / 取消收藏（切换），返回更新后的帖子视图 */
    Map<String, Object> toggleFavorite(Long id, String userKey);

    /** 我的收藏列表（按收藏时间倒序；帖子已被删除的自动跳过） */
    java.util.List<Map<String, Object>> listFavorites(String userKey);

    /** 举报帖子（同一人对同一帖只能举报一次） */
    void addReport(Long postId, String reporter, String reason);

    /** 后台：举报列表（含帖子摘要，待处理的排前面），返回 { items } 供控制器包装 */
    java.util.List<Map<String, Object>> adminListReports();

    /** 后台：处理举报（action = dismiss 驳回 | delete 删帖），返回处理后的举报视图 */
    Map<String, Object> adminHandleReport(Long reportId, String action);
}
