package com.fenglin.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fenglin.springboottest.entity.PetPostComment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PetPostCommentMapper extends BaseMapper<PetPostComment> {
}
