package com.lmj.platformserver.controller;

import com.lmj.platformserver.result.Response;
import com.lmj.platformserver.service.DataBoardService;
import com.lmj.platformserver.vo.DataBoardVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@Slf4j
@RequestMapping("/dataBoard")
public class DataBoardController {

    @Autowired
    private DataBoardService dataBoardService;

    @GetMapping("/getData")
    public Response<DataBoardVo> getData() {
        log.info("获取数据看板数据");
        DataBoardVo data = dataBoardService.getData();
        return Response.success(data);
    }
}
