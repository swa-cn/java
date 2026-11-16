package com.fenglin.springboottest.task;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fenglin.springboottest.entity.Pet;
import com.fenglin.springboottest.mapper.PetMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 宠物状态定时衰减（2026-11 新增）。
 *
 * 此前饱食度/心情只在喂食/互动时增加，永不下降，宠物缺少「需要照顾」的感觉。
 * 现在每小时对全部已孵化宠物：饱食度 -1、心情 -1（下限 0，不判负值）。
 * 节奏参考：饱食度 60 → 约 2.5 天见底；喂一次 +25，足够拉回来。
 *
 * 实现说明：单条 UPDATE 批量衰减（GREATEST 保底 0），不逐只读改写，
 * 宠物量增长也不会拖慢；鸡蛋（status=egg）不衰减。
 */
@Component
public class PetStatusDecayTask {

    private static final Logger log = LoggerFactory.getLogger(PetStatusDecayTask.class);

    private final PetMapper petMapper;

    public PetStatusDecayTask(PetMapper petMapper) {
        this.petMapper = petMapper;
    }

    /** 每小时执行一次（应用启动 5 分钟后先跑第一轮） */
    @Scheduled(fixedDelay = 60 * 60 * 1000, initialDelay = 5 * 60 * 1000)
    public void decayHourly() {
        UpdateWrapper<Pet> uw = new UpdateWrapper<>();
        uw.eq("status", "hatched")
          .setSql("satiety = GREATEST(satiety - 1, 0)")
          .setSql("mood = GREATEST(mood - 1, 0)");
        int rows = petMapper.update(null, uw);
        if (rows > 0) {
            log.info("[宠物衰减] 本轮更新 {} 只宠物的饱食度/心情（各 -1）", rows);
        }
    }
}
