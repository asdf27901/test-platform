package com.lmj.platformserver.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lmj.platformserver.dto.DeleteInterfaceDTO;
import com.lmj.platformserver.dto.InterfacePageQueryDTO;
import com.lmj.platformserver.dto.SaveInterfacesDTO;
import com.lmj.platformserver.groups.Add;
import com.lmj.platformserver.result.Response;
import com.lmj.platformserver.service.InterfaceService;
import com.lmj.platformserver.vo.InterfaceVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@Validated
@RequestMapping("/interface")
public class InterfaceController {

    @Autowired
    private InterfaceService interfaceService;

    @PostMapping("/saveInterfaces")
    public Response<?> saveInterfaces(@RequestBody @Validated(Add.class) SaveInterfacesDTO saveInterfacesDTO) {
        log.info("批量添加接口: {}", saveInterfacesDTO);
        interfaceService.saveInterfaces(saveInterfacesDTO);
        return Response.success();
    }

    @GetMapping("/interfaceList")
    public Response<IPage<InterfaceVo>> getInterfaceList(InterfacePageQueryDTO interfacePageQueryDTO) {
        log.info("获取接口列表");
        IPage<InterfaceVo> interfaceVoIPage = interfaceService.getInterfaceList(interfacePageQueryDTO);
        return Response.success(interfaceVoIPage);
    }

    @PostMapping("/deleteBatch")
    public Response<?> deleteBatch(@RequestBody DeleteInterfaceDTO dto) {
        log.info("批量删除接口");
        interfaceService.deleteBatch(dto.getIds());
        return Response.success();
    }
}
