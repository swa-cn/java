package com.fenglin.springboottest.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.fenglin.springboottest.mapper")
public class MybatisPlusConfig {
}
