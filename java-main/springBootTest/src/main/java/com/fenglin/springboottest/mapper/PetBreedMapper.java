package com.fenglin.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fenglin.springboottest.entity.PetBreed;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PetBreedMapper extends BaseMapper<PetBreed> {
}
