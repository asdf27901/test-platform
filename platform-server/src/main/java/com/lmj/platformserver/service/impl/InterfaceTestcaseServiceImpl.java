package com.lmj.platformserver.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmj.platformserver.assertion.PostAssertionTool;
import com.lmj.platformserver.dto.InterfaceTestcaseListQueryDTO;
import com.lmj.platformserver.entity.Interface;
import com.lmj.platformserver.entity.InterfaceTestcase;
import com.lmj.platformserver.exception.InterfaceErrorException;
import com.lmj.platformserver.exception.InterfaceTestcaseErrorException;
import com.lmj.platformserver.mapper.InterfaceMapper;
import com.lmj.platformserver.mapper.InterfaceTestcaseMapper;
import com.lmj.platformserver.result.ResultCodeEnum;
import com.lmj.platformserver.service.InterfaceTestcaseService;
import com.lmj.platformserver.service.JsScriptExecutionService;
import com.lmj.platformserver.utils.HttpUtil;
import com.lmj.platformserver.vo.InterfaceTestcaseVo;
import com.lmj.platformserver.vo.RequestResultVo;
import com.lmj.platformserver.vo.ScriptExecutionResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class InterfaceTestcaseServiceImpl implements InterfaceTestcaseService {

    @Autowired
    private InterfaceTestcaseMapper interfaceTestcaseMapper;

    @Autowired
    private InterfaceMapper interfaceMapper;

    @Autowired
    private HttpUtil httpUtil;

    @Autowired
    private JsScriptExecutionService jsScriptExecutionService;

    @Override
    public void save(List<InterfaceTestcase> interfaceTestcases, Long interfaceId) {
        Interface i = interfaceMapper.selectById(interfaceId);
        if (i == null) {
            throw new InterfaceErrorException(ResultCodeEnum.INTERFACE_ID_NOT_FOUND);
        }
        interfaceTestcases.forEach(ii -> ii.setInterfaceId(interfaceId));

        interfaceTestcaseMapper.insertOrUpdate(interfaceTestcases);
    }

    @Override
    public IPage<InterfaceTestcaseVo> getInterfaceTestcaseList(InterfaceTestcaseListQueryDTO interfaceTestcaseListQueryDTO) {
        Page<InterfaceTestcaseVo> page = new Page<>(interfaceTestcaseListQueryDTO.getCurrent(), interfaceTestcaseListQueryDTO.getSize());
        return interfaceTestcaseMapper.getInterfaceTestcaseList(page, interfaceTestcaseListQueryDTO);
    }

    @Override
    public void deleteInterfaceTestcaseBatch(List<Long> ids) {
        interfaceTestcaseMapper.deleteByIds(ids);
    }

    @Override
    public InterfaceTestcase getInterfaceTestcaseDetail(Long id) {
        InterfaceTestcase interfaceTestcase = interfaceTestcaseMapper.selectById(id);
        if (interfaceTestcase == null) {
            throw new InterfaceTestcaseErrorException(ResultCodeEnum.INTERFACE_TESTCASE_ID_NOT_FOUND);
        }
        return interfaceTestcase;
    }

    @Override
    public RequestResultVo sendInterfaceTestcaseRequest(Map<String, Object> requestData) {
        Long interfaceId = (Long) requestData.get("interfaceId");
        Interface i = interfaceMapper.selectById(interfaceId);
        if (i == null) {
            throw new InterfaceErrorException(ResultCodeEnum.INTERFACE_ID_NOT_FOUND);
        }

        String path = i.getPath();
        Map<String, Object> response = httpUtil.sendCustomizeHttpRequest(requestData, path);

        RequestResultVo requestResultVo = new RequestResultVo();
        requestResultVo.setResponse(response);

        String postRequestScript = (String) requestData.get("postRequestScript");
        if (postRequestScript != null && !postRequestScript.equals("")) {
            ScriptExecutionResultVo executionResultVo = jsScriptExecutionService.executeJsScript(
                    postRequestScript,
                    new PostAssertionTool(response)
            );
            requestResultVo.setPostExecutionResult(executionResultVo);
        }

        return requestResultVo;
    }
}
