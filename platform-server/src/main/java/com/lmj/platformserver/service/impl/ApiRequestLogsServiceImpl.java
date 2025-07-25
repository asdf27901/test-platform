package com.lmj.platformserver.service.impl;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
            List<Long> testcaseIds = new ArrayList<>();
            List<Long> interfaceIds = new ArrayList<>();
            HashMap<Long, RequestSteps> testcaseIdStepsMap = new HashMap<>();
            HashMap<Long, RequestSteps> interfaceIdStepsMap = new HashMap<>();
            for (RequestSteps step : steps) {
                Long testcaseId = step.getTestcaseId();
                if (testcaseId != null) {
                    testcaseIds.add(testcaseId);
                    testcaseIdStepsMap.put(testcaseId, step);
                }
                Long interfaceId = step.getInterfaceId();
                if (interfaceId != null) {
                    interfaceIds.add(step.getInterfaceId());
                    interfaceIdStepsMap.put(interfaceId, step);
                }
            }
            List<InterfaceTestcase> interfaceTestcases = interfaceTestcaseMapper.selectByIds(testcaseIds);
            for (InterfaceTestcase testcase : interfaceTestcases) {
                RequestSteps step = testcaseIdStepsMap.get(testcase.getId());
                if (step != null) {
                    step.setTestcaseName(testcase.getName());
                }
            }
            List<Interface> interfaces = interfaceMapper.selectByIds(interfaceIds);
            for (Interface i : interfaces) {
                RequestSteps step = interfaceIdStepsMap.get(i.getId());
                if (step != null) {
                    step.setInterfaceName(i.getName());
                }
            }
        }
        return logs;
    }
}
