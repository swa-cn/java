package com.fenglin.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fenglin.springboottest.entity.PetPostReport;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PetPostReportMapper extends BaseMapper<PetPostReport> {
}
