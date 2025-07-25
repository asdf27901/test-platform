package com.lmj.platformserver.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lmj.platformserver.dto.ApiRequestLogsPageQueryDTO;
import com.lmj.platformserver.entity.ApiRequestLogs;
import com.lmj.platformserver.result.Response;
import com.lmj.platformserver.service.ApiRequestLogsService;
import com.lmj.platformserver.vo.ApiRequestLogsPageQueryVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@Slf4j
@RequestMapping("/apiRequestLogs")
public class ApiRequestLogsController {

    @Autowired
    private ApiRequestLogsService apiRequestLogsService;

    @GetMapping("/list")
    public Response<IPage<ApiRequestLogsPageQueryVo>> getApiRequestLogsList(ApiRequestLogsPageQueryDTO dto) {
        log.info("获取接口请求记录");
        IPage<ApiRequestLogsPageQueryVo> iPage = apiRequestLogsService.getApiRequestLogsList(dto);
        return Response.success(iPage);
    }

    @GetMapping("/detail")
    public Response<ApiRequestLogs> getApiRequestLogsDetailById(@RequestParam Long id) {
        log.info("获取接口请求记录详情: {}", id);
        ApiRequestLogs logs = apiRequestLogsService.getApiRequestLogsDetailById(id);
        return Response.success(logs);
    }
}
