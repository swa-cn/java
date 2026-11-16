package com.fenglin.springboottest.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.fenglin.springboottest.mapper")
public class MybatisPlusConfig {
    // 说明：帖子列表分页用 QueryWrapper.last("LIMIT ...") 手动实现（项目仅跑 MySQL），
    // 不引入 mybatis-plus-jsqlparser 分页插件依赖。
}
