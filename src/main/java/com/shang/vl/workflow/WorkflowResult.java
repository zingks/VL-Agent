package com.shang.vl.workflow;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 流程输出结果。
 * Created by shangwei2009@hotmail.com on 2026/3/6 22:23
 */
@Data
@Accessors(chain = true)
public class WorkflowResult {

    private WorkflowMode mode;
    private String requirement;
    private String testCases;
    private String review;
}
