package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.dto.CommentRequest;
import com.fenglin.springboottest.service.PostService;
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
 *  GET    /api/content/posts?category=&userKey=      -> { ok, items: [Post] }
 *  GET    /api/content/posts/{id}?userKey=           -> { ok, post: Post }
 *  POST   /api/content/posts                         multipart: title, content, category,
 *                                                            tags, author, authorName, files[]
 *                                                    -> { ok, post: Post }
 *  DELETE /api/content/posts/{id}?author=            -> { ok }
 *  POST   /api/content/posts/{id}/like?userKey=      -> { ok, post: Post }
 *  POST   /api/content/posts/{id}/comments           { content, author, authorName }
 *                                                    -> { ok, post: Post }
 *
 * userKey 是点赞归属标识（前端传当前登录用户名）；演示级实现未做登录态校验。
 */
@RestController
@RequestMapping("/api/content")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    /** 帖子列表：category 为空返回全部 */
    @GetMapping("/posts")
    public Map<String, Object> list(@RequestParam(required = false) String category,
                                    @RequestParam(required = false) String userKey) {
        List<Map<String, Object>> items = postService.list(category, userKey);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        m.put("items", items);
        return m;
    }

    /** 帖子详情 */
    @GetMapping("/posts/{id}")
    public Map<String, Object> detail(@PathVariable Long id,
                                      @RequestParam(required = false) String userKey) {
        Map<String, Object> post = postService.detail(id, userKey);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", post != null);
        m.put("post", post);
        return m;
    }

    /** 发帖：multipart/form-data，files 可多个（图片 + 视频） */
    @PostMapping("/posts")
    public Map<String, Object> create(@RequestParam(required = false) String title,
                                      @RequestParam(required = false) String content,
                                      @RequestParam(required = false) String category,
                                      @RequestParam(required = false) String tags,
                                      @RequestParam(required = false) String author,
                                      @RequestParam(required = false) String authorName,
                                      @RequestPart(required = false) List<MultipartFile> files) {
        Map<String, Object> post = postService.create(title, content, category, tags,
                author, authorName, files);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        m.put("post", post);
        return m;
    }

    /** 删除帖子（仅作者本人） */
    @DeleteMapping("/posts/{id}")
    public Map<String, Object> delete(@PathVariable Long id,
                                      @RequestParam(required = false) String author) {
        postService.delete(id, author);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        return m;
    }

    /** 点赞 / 取消点赞（按 userKey 切换） */
    @PostMapping("/posts/{id}/like")
    public Map<String, Object> like(@PathVariable Long id,
                                    @RequestParam(required = false) String userKey) {
        Map<String, Object> post = postService.toggleLike(id, userKey);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        m.put("post", post);
        return m;
    }

    /** 发表评论 */
    @PostMapping("/posts/{id}/comments")
    public Map<String, Object> comment(@PathVariable Long id, @RequestBody CommentRequest req) {
        Map<String, Object> post = postService.addComment(
                id, req.getAuthor(), req.getAuthorName(), req.getContent());
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        m.put("post", post);
        return m;
    }
}
