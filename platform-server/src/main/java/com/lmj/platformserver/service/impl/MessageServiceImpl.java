package com.lmj.platformserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmj.platformserver.constant.MessageConstant;
import com.lmj.platformserver.constant.MessageTypeConstant;
import com.lmj.platformserver.context.UserContextHolder;
import com.lmj.platformserver.entity.Message;
import com.lmj.platformserver.mapper.MessageMapper;
import com.lmj.platformserver.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private SimpMessagingTemplate messagingTemplate; // Spring提供的WebSocket消息发送模板

    @Override
    @Transactional
    @Async
    public void sendChainRequestExecuteNotify(String msg, Long userId, String type, String path) {
        if (!StringUtils.hasText(msg) || userId == null) {
            return;
        }
        Message message = new Message();
        message.setContent(msg);
        message.setRecipientId(userId);
        message.setIsRead(MessageConstant.UNREAD);
        message.setType(type == null ? MessageTypeConstant.UNDEFINED : type);
        message.setPath(path);
        messageMapper.insert(message);

        messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/notifications", message);
    }

    @Override
    public List<Message> getRecentMessageList() {
        Long userId = UserContextHolder.getUserId();
        return messageMapper.selectList(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getRecipientId, userId)
                        .orderByDesc(Message::getCreatedTime)
                        .last("limit 15")
        );
    }

    @Override
    public List<Message> getAllUnReadMessage(Long page, Long pageSize) {
        Long userId = UserContextHolder.getUserId();
        IPage<Message> messageIPage = new Page<>(page, pageSize);
        IPage<Message> selectedPage = messageMapper.selectPage(messageIPage,
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getRecipientId, userId)
                        .eq(Message::getIsRead, MessageConstant.UNREAD)
                        .orderByDesc(Message::getCreatedTime)
        );
        return selectedPage.getRecords();
    }

    @Override
    public void markMessageAsRead(List<Long> ids) {
        Long userId = UserContextHolder.getUserId();
        messageMapper.update(
                new LambdaUpdateWrapper<Message>()
                        .set(Message::getIsRead, MessageConstant.READ)
                        .eq(Message::getRecipientId, userId)
                        .in(Message::getId, new HashSet<>(ids))
        );
    }
}
