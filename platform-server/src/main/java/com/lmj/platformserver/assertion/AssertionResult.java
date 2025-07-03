package com.lmj.platformserver.assertion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssertionResult {
    private String testName;
    private Boolean result;
    private String message;
}
