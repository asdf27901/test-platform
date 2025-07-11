package com.lmj.platformserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lmj.platformserver.entity.EnvironmentVariable;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EnvironmentVariableMapper extends BaseMapper<EnvironmentVariable> {
}
