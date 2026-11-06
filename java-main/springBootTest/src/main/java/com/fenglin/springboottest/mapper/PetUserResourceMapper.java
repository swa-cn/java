package com.fenglin.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fenglin.springboottest.entity.PetUserResource;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PetUserResourceMapper extends BaseMapper<PetUserResource> {
}
