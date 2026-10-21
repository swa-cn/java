package com.fenglin.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fenglin.springboottest.entity.PetPost;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PetPostMapper extends BaseMapper<PetPost> {
}
