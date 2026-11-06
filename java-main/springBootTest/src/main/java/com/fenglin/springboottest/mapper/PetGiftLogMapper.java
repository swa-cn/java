package com.fenglin.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fenglin.springboottest.entity.PetGiftLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PetGiftLogMapper extends BaseMapper<PetGiftLog> {
}
