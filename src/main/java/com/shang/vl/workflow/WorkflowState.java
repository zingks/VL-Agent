package com.shang.vl.workflow;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * LangGraph 流程共享状态。
 * Created by shangwei2009@hotmail.com on 2026/3/9 11:10
 */
@Data
@Accessors(chain = true)
public class WorkflowState implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String originalDemand;
    private String requirement;
    private String testCases;
    private List<String> imageUrls;
    private String review;
}
