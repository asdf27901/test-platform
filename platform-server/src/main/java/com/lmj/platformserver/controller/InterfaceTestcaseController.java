package com.lmj.platformserver.controller;

import com.lmj.platformserver.dto.InterfaceTestcaseDTO;
import com.lmj.platformserver.groups.Add;
import com.lmj.platformserver.result.Response;
import com.lmj.platformserver.service.InterfaceTestcaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        interfaceTestcaseService.save(interfaceTestcaseDTO.getInterfaceTestcases());
        return Response.success();
    }
}
