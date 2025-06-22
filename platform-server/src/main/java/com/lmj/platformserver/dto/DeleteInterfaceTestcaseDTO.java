package com.lmj.platformserver.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class DeleteInterfaceTestcaseDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private List<Long> ids;
}
