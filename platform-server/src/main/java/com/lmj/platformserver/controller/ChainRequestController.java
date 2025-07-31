package com.lmj.platformserver.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lmj.platformserver.dto.ChainRequestDTO;
import com.lmj.platformserver.dto.ChainRequestPageQueryDTO;
import com.lmj.platformserver.dto.DeleteChainRequestDTO;
import com.lmj.platformserver.groups.Add;
import com.lmj.platformserver.groups.Update;
import com.lmj.platformserver.result.Response;
import com.lmj.platformserver.service.ChainRequestService;
import com.lmj.platformserver.vo.ChainRequestDetailVo;
import com.lmj.platformserver.vo.ChainRequestPageQueryVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@Slf4j
@RequestMapping("/chainRequest")
public class ChainRequestController {

    @Autowired
    private ChainRequestService chainRequestService;

    @PostMapping("/save")
    public Response<?> saveChainRequest(@RequestBody @Validated(Add.class) ChainRequestDTO dto) {
        log.info("添加链路请求：{}", dto);
        chainRequestService.saveChainRequest(dto);
        return Response.success();
    }

    @GetMapping("/list")
    public Response<IPage<ChainRequestPageQueryVo>> getChainRequestPageQueryVoList(ChainRequestPageQueryDTO dto) {
        log.info("获取链路请求列表");
        IPage<ChainRequestPageQueryVo> voIPage = chainRequestService.getChainRequestPageQueryVoList(dto);
        return Response.success(voIPage);
    }

    @PostMapping("/delete")
    public Response<?> deleteChainRequest(@RequestBody @Validated DeleteChainRequestDTO dto) {
        log.info("删除链路请求: {}", dto.getIds());
        chainRequestService.deleteChainRequest(dto.getIds());
        return Response.success();
    }

    @GetMapping("/detail")
    public Response<ChainRequestDetailVo> getChainRequestDetail(@RequestParam Long id) {
        log.info("获取链路请求详情：{}", id);
        ChainRequestDetailVo chainRequestDetailVo = chainRequestService.getChainRequestDetail(id);
        return Response.success(chainRequestDetailVo);
    }

    @PostMapping("/update")
    public Response<?> updateChainRequest(@RequestBody @Validated(Update.class) ChainRequestDTO dto) {
        log.info("更新链路请求：{}", dto);
        chainRequestService.updateChainRequest(dto);
        return Response.success();
    }
}
