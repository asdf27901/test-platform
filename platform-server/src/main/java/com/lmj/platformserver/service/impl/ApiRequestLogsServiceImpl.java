package com.lmj.platformserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmj.platformserver.dto.ApiRequestLogsPageQueryDTO;
import com.lmj.platformserver.entity.ApiRequestLogs;
import com.lmj.platformserver.entity.Interface;
import com.lmj.platformserver.entity.InterfaceTestcase;
import com.lmj.platformserver.exception.ApiRequestLogsErrorException;
import com.lmj.platformserver.mapper.ApiRequestLogsMapper;
import com.lmj.platformserver.mapper.InterfaceMapper;
import com.lmj.platformserver.mapper.InterfaceTestcaseMapper;
import com.lmj.platformserver.pojo.RequestSteps;
import com.lmj.platformserver.result.ResultCodeEnum;
import com.lmj.platformserver.service.ApiRequestLogsService;
import com.lmj.platformserver.vo.ApiRequestLogsPageQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ApiRequestLogsServiceImpl implements ApiRequestLogsService {

    @Autowired
    private ApiRequestLogsMapper apiRequestLogsMapper;

    @Autowired
    private InterfaceTestcaseMapper interfaceTestcaseMapper;

    @Autowired
    private InterfaceMapper interfaceMapper;

    @Override
    public IPage<ApiRequestLogsPageQueryVo> getApiRequestLogsList(ApiRequestLogsPageQueryDTO dto) {
        Page<ApiRequestLogsPageQueryVo> voPage = new Page<>(dto.getCurrent(), dto.getSize());
        return apiRequestLogsMapper.getApiRequestLogsList(voPage, dto);
    }

    @Override
    public ApiRequestLogs getApiRequestLogsDetailById(Long id) {
        ApiRequestLogs logs = apiRequestLogsMapper.selectById(id);
        if (logs == null) {
            throw new ApiRequestLogsErrorException(ResultCodeEnum.API_REQUEST_LOGS_ID_NOT_FOUND);
        }
        List<RequestSteps> steps = logs.getSteps();
        if (steps != null) {
            Set<Long> testcaseIds = new HashSet<>();
            Set<Long> interfaceIds = new HashSet<>();

            for (RequestSteps step : steps) {
                Long testcaseId = step.getTestcaseId();
                if (testcaseId != null) {
                    testcaseIds.add(testcaseId);
                }
                Long interfaceId = step.getInterfaceId();
                if (interfaceId != null) {
                    interfaceIds.add(step.getInterfaceId());
                }
            }
            Map<String, String> testcaseNameMap = null;
            Map<String, String> interfaceNameMap = null;
            if (testcaseIds.size() > 0 ){
                List<Map<String, Object>> testcaseMapList = interfaceTestcaseMapper.selectMaps(
                        new LambdaQueryWrapper<InterfaceTestcase>()
                                .select(InterfaceTestcase::getId, InterfaceTestcase::getName)
                                .in(InterfaceTestcase::getId, testcaseIds)
                );
                if (!testcaseMapList.isEmpty()) {
                    testcaseNameMap = testcaseMapList.stream()
                            .collect(Collectors.toMap(t -> t.get("id").toString(), t -> (String) t.get("name")));
                }
            }
            if (interfaceIds.size() > 0) {
                List<Map<String, Object>> interfaceMapList = interfaceMapper.selectMaps(
                        new LambdaQueryWrapper<Interface>()
                                .select(Interface::getId, Interface::getName)
                                .in(Interface::getId, interfaceIds)
                );
                if (!interfaceMapList.isEmpty()) {
                    interfaceNameMap = interfaceMapList.stream()
                            .collect(Collectors.toMap(t -> t.get("id").toString(), t -> (String) t.get("name")));
                }
            }
            for (RequestSteps step : steps) {
                Long testcaseId = step.getTestcaseId();
                if (testcaseId != null && testcaseNameMap != null) {
                    step.setTestcaseName(testcaseNameMap.get(testcaseId.toString()));
                }
                Long interfaceId = step.getInterfaceId();
                if (interfaceId != null && interfaceNameMap != null) {
                    step.setInterfaceName(interfaceNameMap.get(interfaceId.toString()));
                }
            }
        }
        return logs;
    }
}
