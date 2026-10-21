package com.fenglin.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fenglin.springboottest.entity.PetCategory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PetCategoryMapper extends BaseMapper<PetCategory> {
}
