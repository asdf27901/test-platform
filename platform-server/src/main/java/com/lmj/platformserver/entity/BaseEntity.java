package com.lmj.platformserver.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.lmj.platformserver.groups.Update;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class BaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @NotNull(groups = Update.class, message = "ID不能为空")
    private Long id;

    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    @TableField(value = "is_deleted")
    private Byte deleted;

    @Version
    private Long version;
}
