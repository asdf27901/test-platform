package com.lmj.platformserver.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lmj.platformserver.dto.DeleteInterfaceTestcaseDTO;
import com.lmj.platformserver.dto.InterfaceTestcaseDTO;
import com.lmj.platformserver.dto.InterfaceTestcaseListQueryDTO;
import com.lmj.platformserver.entity.InterfaceTestcase;
import com.lmj.platformserver.groups.Add;
import com.lmj.platformserver.result.Response;
import com.lmj.platformserver.service.InterfaceTestcaseService;
import com.lmj.platformserver.vo.InterfaceTestcaseVo;
import com.lmj.platformserver.vo.RequestResultVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Slf4j
@Validated
@RequestMapping("/interface/testcase")
public class InterfaceTestcaseController {

    @Autowired
    private InterfaceTestcaseService interfaceTestcaseService;

    @PostMapping("/save")
    public Response<?> saveInterfaceTestcase(@RequestBody @Validated(Add.class) InterfaceTestcaseDTO interfaceTestcaseDTO) {
        log.info("添加接口测试用例：{}", interfaceTestcaseDTO);
        interfaceTestcaseService.save(interfaceTestcaseDTO.getInterfaceTestcases(), interfaceTestcaseDTO.getInterfaceId());
        return Response.success();
    }

    @GetMapping("/list")
    public Response<IPage<InterfaceTestcaseVo>> getInterfaceTestcaseList(InterfaceTestcaseListQueryDTO interfaceTestcaseListQueryDTO) {
        log.info("获取接口用例列表");
        IPage<InterfaceTestcaseVo> voIPage = interfaceTestcaseService.getInterfaceTestcaseList(interfaceTestcaseListQueryDTO);
        return Response.success(voIPage);
    }

    @PostMapping("/delete")
    public Response<?> deleteInterfaceTestcase(@RequestBody DeleteInterfaceTestcaseDTO dto) {
        log.info("删除接口用例");
        interfaceTestcaseService.deleteInterfaceTestcaseBatch(dto.getIds());
        return Response.success();
    }

    @GetMapping("/detail")
    public Response<InterfaceTestcase> getInterfaceTestcaseDetail(Long id) {
        log.info("获取接口测试用例详情: {}", id);
        InterfaceTestcase interfaceTestcase = interfaceTestcaseService.getInterfaceTestcaseDetail(id);
        return Response.success(interfaceTestcase);
    }

    @PostMapping("/sendRequest")
    public Response<RequestResultVo> sendTestcaseRequest(
            @RequestBody @Validated InterfaceTestcase interfaceTestcase,
            @RequestParam(value = "envId", required = false) Long envId
    ) {
        log.info("发送测试用例请求");
        Map<String, Object> requestData = BeanUtil.beanToMap(interfaceTestcase);
        RequestResultVo resultVo = interfaceTestcaseService.sendInterfaceTestcaseRequest(requestData, envId);
        return Response.success(resultVo);
    }
 }
