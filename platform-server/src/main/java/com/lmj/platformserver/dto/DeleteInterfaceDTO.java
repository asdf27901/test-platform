package com.lmj.platformserver.dto;

import lombok.Data;

import java.util.List;

@Data
public class DeleteInterfaceDTO {
    private List<Long> ids;
}
