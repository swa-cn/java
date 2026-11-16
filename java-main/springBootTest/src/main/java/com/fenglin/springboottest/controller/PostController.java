package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.auth.CurrentUser;
import com.fenglin.springboottest.dto.CommentRequest;
import com.fenglin.springboottest.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 「发现」帖子接口（与前端 login-vue/src/api/post.js 的约定一一对应）：
 *
 *  GET    /api/content/posts?category=&author=&page=&size=   -> { ok, items, total, page, size, hasMore }
 *  GET    /api/content/posts/{id}                            -> { ok, post: Post }
 *  POST   /api/content/posts                                 multipart: title, content, category,
 *                                                             tags, authorName, files[]
 *                                                             -> { ok, post: Post }
 *  DELETE /api/content/posts/{id}                            -> { ok }
 *  POST   /api/content/posts/{id}/like                       -> { ok, post: Post }
 *  POST   /api/content/posts/{id}/comments                   { content, authorName } -> { ok, post: Post }
 *
 * 身份（2026-11 起）：写操作（发帖/删帖/点赞/评论）一律取登录会话用户，
 * 前端传的 author/userKey 仅作兼容保留、一律忽略 —— 无法再冒充他人操作。
 * author 查询参数（「我的发帖记录」）也只允许查自己的：传入时强制覆盖为会话用户。
 */
@RestController
@RequestMapping("/api/content")
public class PostController {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 50;

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    /** 帖子分页列表：category 为空返回全部分类；author 仅接受当前登录用户（查自己的帖子） */
    @GetMapping("/posts")
    public Map<String, Object> list(@RequestParam(required = false) String category,
                                    @RequestParam(required = false) String author,
                                    @RequestParam(required = false) Integer page,
                                    @RequestParam(required = false) Integer size,
                                    HttpServletRequest request) {
        String me = CurrentUser.get(request);
        // author 查询是"我的发帖记录"场景：必须登录，且只能查自己的
        if (author != null && !author.isBlank()) {
            author = CurrentUser.require(request);
        }
        int pageNo = page == null || page < 1 ? 1 : page;
        int pageSize = size == null || size < 1 ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);
        Map<String, Object> data = postService.page(category, author, me, pageNo, pageSize);
        long total = data.get("total") instanceof Number n ? n.longValue() : 0;

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        m.put("items", data.get("items"));
        m.put("total", total);
        m.put("page", pageNo);
        m.put("size", pageSize);
        m.put("hasMore", (long) pageNo * pageSize < total);
        return m;
    }

    /** 帖子详情（点赞状态按登录用户计算） */
    @GetMapping("/posts/{id}")
    public Map<String, Object> detail(@PathVariable Long id, HttpServletRequest request) {
        Map<String, Object> post = postService.detail(id, CurrentUser.get(request));
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", post != null);
        m.put("post", post);
        return m;
    }

    /** 发帖：multipart/form-data，files 可多个（图片 + 视频）；作者 = 登录用户 */
    @PostMapping("/posts")
    public Map<String, Object> create(@RequestParam(required = false) String title,
                                      @RequestParam(required = false) String content,
                                      @RequestParam(required = false) String category,
                                      @RequestParam(required = false) String tags,
                                      @RequestParam(required = false) String authorName,
                                      @RequestPart(required = false) List<MultipartFile> files,
                                      HttpServletRequest request) {
        String me = CurrentUser.require(request);
        Map<String, Object> post = postService.create(title, content, category, tags,
                me, authorName, files);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        m.put("post", post);
        return m;
    }

    /** 删除帖子（仅作者本人，作者取登录会话） */
    @DeleteMapping("/posts/{id}")
    public Map<String, Object> delete(@PathVariable Long id, HttpServletRequest request) {
        postService.delete(id, CurrentUser.require(request));
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        return m;
    }

    /** 点赞 / 取消点赞（点赞人 = 登录用户） */
    @PostMapping("/posts/{id}/like")
    public Map<String, Object> like(@PathVariable Long id, HttpServletRequest request) {
        Map<String, Object> post = postService.toggleLike(id, CurrentUser.require(request));
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        m.put("post", post);
        return m;
    }

    /** 发表评论（评论人 = 登录用户；replyToId 非空 = 楼中楼回复） */
    @PostMapping("/posts/{id}/comments")
    public Map<String, Object> comment(@PathVariable Long id,
                                       @RequestBody CommentRequest req,
                                       HttpServletRequest request) {
        String me = CurrentUser.require(request);
        Map<String, Object> post = postService.addComment(
                id, me, req.getAuthorName(), req.getContent(), req.getReplyToId());
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        m.put("post", post);
        return m;
    }

    /** 收藏 / 取消收藏（切换，收藏人 = 登录用户） */
    @PostMapping("/posts/{id}/favorite")
    public Map<String, Object> favorite(@PathVariable Long id, HttpServletRequest request) {
        Map<String, Object> post = postService.toggleFavorite(id, CurrentUser.require(request));
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        m.put("post", post);
        return m;
    }

    /** 我的收藏列表 */
    @GetMapping("/favorites")
    public Map<String, Object> favorites(HttpServletRequest request) {
        var items = postService.listFavorites(CurrentUser.require(request));
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        m.put("items", items);
        return m;
    }

    /** 举报帖子：POST /api/content/posts/{id}/report { reason }（举报人 = 登录用户） */
    @PostMapping("/posts/{id}/report")
    public Map<String, Object> report(@PathVariable Long id,
                                      @RequestBody Map<String, String> body,
                                      HttpServletRequest request) {
        postService.addReport(id, CurrentUser.require(request),
                body == null ? null : body.get("reason"));
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        m.put("message", "举报成功，管理员会尽快处理");
        return m;
    }
}
