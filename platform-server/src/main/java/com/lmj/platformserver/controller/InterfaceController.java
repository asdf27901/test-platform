package com.lmj.platformserver.controller;

import com.lmj.platformserver.dto.SaveInterfacesDTO;
import com.lmj.platformserver.result.Response;
import com.lmj.platformserver.service.InterfaceService;
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
@RequestMapping("/interface")
public class InterfaceController {

    @Autowired
    private InterfaceService interfaceService;

    @PostMapping("/saveInterfaces")
    public Response<?> saveInterfaces(@RequestBody @Validated SaveInterfacesDTO saveInterfacesDTO) {
        log.info("批量添加接口: {}", saveInterfacesDTO);
        interfaceService.saveInterfaces(saveInterfacesDTO);
        return Response.success();
    }
}
