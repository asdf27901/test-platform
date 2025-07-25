package com.lmj.platformserver.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lmj.platformserver.dto.ApiRequestLogsPageQueryDTO;
import com.lmj.platformserver.entity.ApiRequestLogs;
import com.lmj.platformserver.vo.ApiRequestLogsPageQueryVo;

public interface ApiRequestLogsService {

    IPage<ApiRequestLogsPageQueryVo> getApiRequestLogsList(ApiRequestLogsPageQueryDTO dto);

    ApiRequestLogs getApiRequestLogsDetailById(Long id);
}
