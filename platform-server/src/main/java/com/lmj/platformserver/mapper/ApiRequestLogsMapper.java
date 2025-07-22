package com.lmj.platformserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmj.platformserver.dto.ApiRequestLogsPageQueryDTO;
import com.lmj.platformserver.entity.ApiRequestLogs;
import com.lmj.platformserver.vo.ApiRequestLogsPageQueryVo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ApiRequestLogsMapper extends BaseMapper<ApiRequestLogs> {
    IPage<ApiRequestLogsPageQueryVo> getApiRequestLogsList(Page<ApiRequestLogsPageQueryVo> voPage, ApiRequestLogsPageQueryDTO dto);
}
