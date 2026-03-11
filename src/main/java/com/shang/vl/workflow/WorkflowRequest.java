package com.shang.vl.workflow;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 流程输入参数。
 * Created by shangwei2009@hotmail.com on 2026/3/6 22:22
 */
@Data
@Accessors(chain = true)
public class WorkflowRequest {

    private List<String> imageUrls;
    private String originalDemand;
    private WorkflowMode mode = WorkflowMode.FULL_PIPELINE;
}
