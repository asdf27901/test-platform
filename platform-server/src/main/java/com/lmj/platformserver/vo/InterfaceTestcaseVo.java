package com.lmj.platformserver.vo;

import com.lmj.platformserver.entity.InterfaceTestcase;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class InterfaceTestcaseVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private InterfaceTestcase interfaceTestcase;

    private Long envId;

    private String path;
}