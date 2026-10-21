package com.fenglin.springboottest.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * 帖子媒体静态资源映射：/uploads/posts/** -> 磁盘目录 {post.upload.dir}。
 *
 * 演示环境直接由后端对外提供文件；生产环境应改为对象存储（OSS/COS）+ CDN，
 * 后端只保存 URL，不再自己承载静态文件。
 */
@Configuration
public class WebResourceConfig implements WebMvcConfigurer {

    @Value("${post.upload.root:uploads}")
    private String uploadRoot;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 把 /uploads/** 映射到「根目录」：URL 里的相对路径（如 posts/xxx.png）
        // 会拼到根目录后面。location 必须以 / 结尾，否则 Spring 按单个文件解析导致 404。
        String root = Paths.get(uploadRoot).toAbsolutePath().normalize().toUri().toString();
        if (!root.endsWith("/")) {
            root = root + "/";
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(root);
    }
}
