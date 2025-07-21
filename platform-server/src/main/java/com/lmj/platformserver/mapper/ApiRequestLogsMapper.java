package com.lmj.platformserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lmj.platformserver.entity.ApiRequestLogs;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ApiRequestLogsMapper extends BaseMapper<ApiRequestLogs> {
}
