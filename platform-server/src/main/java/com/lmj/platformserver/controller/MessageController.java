package com.lmj.platformserver.controller;

import com.lmj.platformserver.entity.Message;
import com.lmj.platformserver.result.Response;
import com.lmj.platformserver.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @GetMapping("/getRecentMessageList")
    public Response<List<Message>> getRecentMessageList() {
        log.info("获取最近的消息通知");
        List<Message> messages = messageService.getRecentMessageList();
        return Response.success(messages);
    }

    @GetMapping("/getAllUnReadMessage")
    public Response<List<Message>> getAllUnReadMessage(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long pageSize
    ) {
        log.info("获取所有未读消息通知");
        List<Message> messages = messageService.getAllUnReadMessage(page, pageSize);
        return Response.success(messages);
    }

    @PostMapping("/markMessageAsRead")
    public Response<?> markMessageAsRead(@RequestParam List<Long> ids) {
        log.info("标记为已读消息: {}", ids);
        messageService.markMessageAsRead(ids);
        return Response.success();
    }
}
