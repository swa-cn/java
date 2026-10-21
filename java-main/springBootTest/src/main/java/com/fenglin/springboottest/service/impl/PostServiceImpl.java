package com.fenglin.springboottest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fenglin.springboottest.entity.PetPost;
import com.fenglin.springboottest.entity.PetPostComment;
import com.fenglin.springboottest.entity.PetPostLike;
import com.fenglin.springboottest.entity.PetPostMedia;
import com.fenglin.springboottest.exception.AuthException;
import com.fenglin.springboottest.mapper.PetPostCommentMapper;
import com.fenglin.springboottest.mapper.PetPostLikeMapper;
import com.fenglin.springboottest.mapper.PetPostMapper;
import com.fenglin.springboottest.mapper.PetPostMediaMapper;
import com.fenglin.springboottest.service.PostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * 帖子业务实现。
 *
 * 媒体文件：保存在 post.upload.dir（默认 ./uploads/posts），文件名用 UUID 防碰撞，
 * 通过 WebResourceConfig 把 /uploads/** 映射为静态资源对外提供访问。
 *
 * 安全面（务必知晓，当前为演示级）：
 *  - author 由前端透传，未绑定登录态 —— 生产必须改为取会话用户；
 *  - 文件按扩展名白名单校验 + 固定重命名，杜绝路径穿越与可执行文件上传；
 *  - 文本内容入库前做长度截断，输出侧由前端纯文本渲染（Vue 插值转义）。
 */
@Service
public class PostServiceImpl implements PostService {

    private static final Logger log = LoggerFactory.getLogger(PostServiceImpl.class);

    private static final List<String> ALLOWED_EXT = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "webp", "bmp", "mp4", "webm", "mov", "m4v");
    private static final long MAX_IMAGE = 5L * 1024 * 1024;
    private static final long MAX_VIDEO = 60L * 1024 * 1024;
    private static final int MAX_IMAGES = 9;
    private static final int MAX_VIDEOS = 1;
    private static final List<String> CATEGORIES = Arrays.asList("cat", "dog", "small", "bird", "fish");

    private final PetPostMapper postMapper;
    private final PetPostMediaMapper mediaMapper;
    private final PetPostLikeMapper likeMapper;
    private final PetPostCommentMapper commentMapper;

    /** 媒体根目录（同时是 /uploads/** 的静态映射根） */
    @Value("${post.upload.root:uploads}")
    private String uploadRoot;

    /** 帖子媒体子目录：磁盘上的 <root>/<subdir>，对外 URL 为 /uploads/<subdir>/xxx */
    @Value("${post.upload.subdir:posts}")
    private String uploadSubdir;

    public PostServiceImpl(PetPostMapper postMapper, PetPostMediaMapper mediaMapper,
                           PetPostLikeMapper likeMapper, PetPostCommentMapper commentMapper) {
        this.postMapper = postMapper;
        this.mediaMapper = mediaMapper;
        this.likeMapper = likeMapper;
        this.commentMapper = commentMapper;
    }

    /* ================= 查询 ================= */

    @Override
    public List<Map<String, Object>> list(String category, String userKey) {
        LambdaQueryWrapper<PetPost> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(category)) {
            qw.eq(PetPost::getCategory, category);
        }
        qw.orderByDesc(PetPost::getCreatedAt).orderByDesc(PetPost::getId);
        List<PetPost> posts = postMapper.selectList(qw);
        List<Map<String, Object>> out = new ArrayList<>(posts.size());
        for (PetPost p : posts) {
            out.add(toView(p, userKey));
        }
        return out;
    }

    @Override
    public Map<String, Object> detail(Long id, String userKey) {
        PetPost p = postMapper.selectById(id);
        return p == null ? null : toView(p, userKey);
    }

    /* ================= 发帖 ================= */

    @Override
    public Map<String, Object> create(String title, String content, String category, String tags,
                                      String author, String authorName, List<MultipartFile> files) {
        String t = trimTo(title, 80);
        String c = trimTo(content, 5000);
        String a = trimTo(author, 20);
        if (!StringUtils.hasText(c) && isEmptyFiles(files)) {
            throw bad(400, "bad_request", "正文和媒体不能同时为空");
        }
        if (!StringUtils.hasText(a)) {
            throw bad(400, "bad_request", "缺少作者信息");
        }
        String cat = CATEGORIES.contains(category) ? category : "cat";

        PetPost post = new PetPost();
        post.setTitle(t);
        post.setContent(c);
        post.setCategory(cat);
        post.setTags(normalizeTags(tags));
        post.setAuthor(a);
        post.setAuthorName(trimTo(authorName, 24));
        post.setCreatedAt(LocalDateTime.now());
        postMapper.insert(post);

        saveMedia(post.getId(), files);
        return toView(postMapper.selectById(post.getId()), a);
    }

    /** 保存上传文件：类型/数量/体积校验 → UUID 重命名 → 落盘 → 写 pet_post_media */
    private void saveMedia(Long postId, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return;
        }
        int images = 0;
        int videos = 0;
        int sortNo = 0;
        try {
            Path dir = mediaDir();
            Files.createDirectories(dir);
            for (MultipartFile f : files) {
                if (f == null || f.isEmpty()) {
                    continue;
                }
                String ext = extOf(f.getOriginalFilename());
                if (!ALLOWED_EXT.contains(ext)) {
                    throw bad(400, "bad_media", "不支持的文件类型：" + f.getOriginalFilename());
                }
                String type = ("mp4".equals(ext) || "webm".equals(ext) || "mov".equals(ext) || "m4v".equals(ext))
                        ? "video" : "image";
                long max = "video".equals(type) ? MAX_VIDEO : MAX_IMAGE;
                if (f.getSize() > max) {
                    throw bad(413, "media_too_large",
                            "文件超过 " + (max / 1024 / 1024) + "MB 限制：" + f.getOriginalFilename());
                }
                if ("image".equals(type)) {
                    if (++images > MAX_IMAGES) {
                        throw bad(400, "too_many_media", "图片最多 " + MAX_IMAGES + " 张");
                    }
                } else {
                    if (++videos > MAX_VIDEOS) {
                        throw bad(400, "too_many_media", "视频最多 " + MAX_VIDEOS + " 个");
                    }
                }
                String stored = UUID.randomUUID().toString().replace("-", "") + "." + ext;
                Path target = dir.resolve(stored).normalize();
                if (!target.startsWith(dir)) {
                    throw bad(400, "bad_media", "非法文件路径");
                }
                try (var in = f.getInputStream()) {
                    Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                }

                PetPostMedia m = new PetPostMedia();
                m.setPostId(postId);
                m.setMediaType(type);
                m.setUrl("/uploads/" + uploadSubdir + "/" + stored);
                m.setName(trimTo(f.getOriginalFilename(), 200));
                m.setSizeBytes(f.getSize());
                m.setSortNo(++sortNo);
                mediaMapper.insert(m);
            }
        } catch (IOException e) {
            log.error("保存帖子媒体失败：{}", e.getMessage(), e);
            throw bad(500, "media_save_failed", "媒体文件保存失败，请稍后再试");
        }
    }

    /* ================= 删除 ================= */

    @Override
    public void delete(Long id, String author) {
        PetPost post = requirePost(id);
        // 归属校验：只允许作者本人删除（author 由调用方透传，生产应取会话用户）
        if (StringUtils.hasText(author) && !author.equalsIgnoreCase(post.getAuthor())) {
            throw bad(403, "forbidden", "只能删除自己的帖子");
        }
        // 清理磁盘上的媒体文件（失败仅记录，不阻断删除）
        List<PetPostMedia> medias = mediaMapper.selectList(
                new LambdaQueryWrapper<PetPostMedia>().eq(PetPostMedia::getPostId, id));
        for (PetPostMedia m : medias) {
            try {
                Path dir = mediaDir();
                Path f = Paths.get(m.getUrl()).getFileName();
                if (f != null) {
                    Files.deleteIfExists(dir.resolve(f).normalize());
                }
            } catch (Exception e) {
                log.warn("清理媒体文件失败 url={}：{}", m.getUrl(), e.getMessage());
            }
        }
        mediaMapper.delete(new LambdaQueryWrapper<PetPostMedia>().eq(PetPostMedia::getPostId, id));
        likeMapper.delete(new LambdaQueryWrapper<PetPostLike>().eq(PetPostLike::getPostId, id));
        commentMapper.delete(new LambdaQueryWrapper<PetPostComment>().eq(PetPostComment::getPostId, id));
        postMapper.deleteById(id);
    }

    /* ================= 点赞 / 评论 ================= */

    @Override
    public Map<String, Object> toggleLike(Long id, String userKey) {
        requirePost(id);
        String key = trimTo(userKey, 20);
        if (!StringUtils.hasText(key)) {
            throw bad(400, "bad_request", "缺少用户标识");
        }
        PetPostLike exist = likeMapper.selectOne(new LambdaQueryWrapper<PetPostLike>()
                .eq(PetPostLike::getPostId, id)
                .eq(PetPostLike::getUserKey, key));
        if (exist != null) {
            likeMapper.deleteById(exist.getId());
        } else {
            PetPostLike like = new PetPostLike();
            like.setPostId(id);
            like.setUserKey(key);
            like.setCreatedAt(LocalDateTime.now());
            likeMapper.insert(like);
        }
        return toView(postMapper.selectById(id), key);
    }

    @Override
    public Map<String, Object> addComment(Long id, String author, String authorName, String content) {
        requirePost(id);
        String text = trimTo(content, 300);
        if (!StringUtils.hasText(text)) {
            throw bad(400, "bad_request", "评论内容不能为空");
        }
        PetPostComment c = new PetPostComment();
        c.setPostId(id);
        c.setAuthor(trimTo(author, 20));
        c.setAuthorName(trimTo(authorName, 24));
        c.setContent(text);
        c.setCreatedAt(LocalDateTime.now());
        commentMapper.insert(c);
        return toView(postMapper.selectById(id), author);
    }

    /* ================= 视图组装 ================= */

    private Map<String, Object> toView(PetPost p, String userKey) {
        List<PetPostMedia> medias = mediaMapper.selectList(new LambdaQueryWrapper<PetPostMedia>()
                .eq(PetPostMedia::getPostId, p.getId())
                .orderByAsc(PetPostMedia::getSortNo));
        List<PetPostLike> likes = likeMapper.selectList(new LambdaQueryWrapper<PetPostLike>()
                .eq(PetPostLike::getPostId, p.getId()));
        List<PetPostComment> comments = commentMapper.selectList(new LambdaQueryWrapper<PetPostComment>()
                .eq(PetPostComment::getPostId, p.getId())
                .orderByAsc(PetPostComment::getCreatedAt)
                .orderByAsc(PetPostComment::getId));

        List<Map<String, Object>> mediaList = new ArrayList<>(medias.size());
        for (PetPostMedia m : medias) {
            Map<String, Object> mm = new LinkedHashMap<>();
            mm.put("type", m.getMediaType());
            mm.put("src", m.getUrl());
            mm.put("name", m.getName());
            mm.put("size", m.getSizeBytes());
            mediaList.add(mm);
        }

        List<String> likeKeys = new ArrayList<>(likes.size());
        for (PetPostLike l : likes) {
            likeKeys.add(l.getUserKey());
        }

        List<Map<String, Object>> commentList = new ArrayList<>(comments.size());
        for (PetPostComment c : comments) {
            Map<String, Object> cm = new LinkedHashMap<>();
            cm.put("id", String.valueOf(c.getId()));
            cm.put("author", c.getAuthor());
            cm.put("authorName", c.getAuthorName());
            cm.put("content", c.getContent());
            cm.put("createdAt", toMillis(c.getCreatedAt()));
            commentList.add(cm);
        }

        Map<String, Object> v = new LinkedHashMap<>();
        v.put("id", String.valueOf(p.getId()));
        v.put("title", p.getTitle());
        v.put("content", p.getContent());
        v.put("category", p.getCategory());
        v.put("tags", splitTags(p.getTags()));
        v.put("author", p.getAuthor());
        v.put("authorName", p.getAuthorName());
        v.put("createdAt", toMillis(p.getCreatedAt()));
        v.put("media", mediaList);
        v.put("likes", likeKeys);
        v.put("comments", commentList);
        return v;
    }

    /** 媒体实际落盘目录：<root>/<subdir>（与 URL 前缀 /uploads/<subdir>/ 一一对应） */
    private Path mediaDir() {
        return Paths.get(uploadRoot, uploadSubdir).toAbsolutePath().normalize();
    }

    private PetPost requirePost(Long id) {
        PetPost p = postMapper.selectById(id);
        if (p == null) {
            throw bad(404, "not_found", "帖子不存在或已删除");
        }
        return p;
    }

    /* ================= 工具 ================= */

    private static Long toMillis(LocalDateTime t) {
        return t == null ? null : t.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private static List<String> splitTags(String csv) {
        List<String> out = new ArrayList<>();
        if (!StringUtils.hasText(csv)) {
            return out;
        }
        for (String s : csv.split(",")) {
            if (StringUtils.hasText(s)) {
                out.add(s.trim());
            }
        }
        return out;
    }

    private static String normalizeTags(String tags) {
        List<String> list = splitTags(tags);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size() && i < 6; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(list.get(i));
        }
        return sb.toString();
    }

    private static String extOf(String filename) {
        String name = filename == null ? "" : filename.toLowerCase(Locale.ROOT);
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) {
            return "";
        }
        return name.substring(dot + 1);
    }

    private static String trimTo(String s, int max) {
        String v = s == null ? null : s.trim();
        if (v == null || v.isEmpty()) {
            return "";
        }
        return v.length() > max ? v.substring(0, max) : v;
    }

    private static boolean isEmptyFiles(List<MultipartFile> files) {
        return files == null || files.stream().allMatch(f -> f == null || f.isEmpty());
    }

    private static AuthException bad(int status, String error, String message) {
        return new AuthException(error, message, status);
    }
}
