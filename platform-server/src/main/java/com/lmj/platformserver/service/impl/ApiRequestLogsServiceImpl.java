package com.lmj.platformserver.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmj.platformserver.dto.ApiRequestLogsPageQueryDTO;
import com.lmj.platformserver.mapper.ApiRequestLogsMapper;
import com.lmj.platformserver.service.ApiRequestLogsService;
import com.lmj.platformserver.vo.ApiRequestLogsPageQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApiRequestLogsServiceImpl implements ApiRequestLogsService {

    @Autowired
    private ApiRequestLogsMapper apiRequestLogsMapper;

    @Override
    public IPage<ApiRequestLogsPageQueryVo> getApiRequestLogsList(ApiRequestLogsPageQueryDTO dto) {
        Page<ApiRequestLogsPageQueryVo> voPage = new Page<>(dto.getCurrent(), dto.getSize());
        return apiRequestLogsMapper.getApiRequestLogsList(voPage, dto);
    }
}
