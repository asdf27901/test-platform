package com.lmj.platformserver.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.lmj.platformserver.entity.InterfaceTestcase;
import com.lmj.platformserver.groups.Add;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class InterfaceTestcaseDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "关联的接口ID不能为空", groups = Add.class)
    @TableField("interface_id")
    private Long interfaceId;

    @Valid
    @NotNull(message = "用例为空")
    @NotEmpty(message = "用例为空")
    private List<InterfaceTestcase> interfaceTestcases;

}
