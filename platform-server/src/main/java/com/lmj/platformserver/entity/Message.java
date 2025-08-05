package com.lmj.platformserver.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("messages")
public class Message {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String content;

    @TableField("recipient_id")
    private Long recipientId;

    @TableField("is_read")
    private Byte isRead;

    private String type;

    private String path;

    @TableField(value = "created_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "MM/dd ")
    private LocalDateTime createdTime;
}
