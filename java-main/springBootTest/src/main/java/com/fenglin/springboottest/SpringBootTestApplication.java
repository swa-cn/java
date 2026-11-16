package com.fenglin.springboottest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling // 开启定时任务（PetStatusDecayTask 宠物状态每小时衰减）
@SpringBootApplication
public class SpringBootTestApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootTestApplication.class, args);
	}

}
