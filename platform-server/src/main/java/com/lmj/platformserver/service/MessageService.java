package com.lmj.platformserver.service;

import com.lmj.platformserver.entity.Message;

import java.util.List;

public interface MessageService {

    void sendChainRequestExecuteNotify(String msg, Long userId, String type, String path);

    List<Message> getRecentMessageList();

    List<Message> getAllUnReadMessage(Long page, Long pageSize);

    void markMessageAsRead(List<Long> ids);
}
