package com.shang.vl.workflow;

/**
 * 需求到测试用例流程编排接口。
 * 先提供顺序实现，后续可替换为 LangGraph 实现。
 * Created by shangwei2009@hotmail.com on 2026/3/6 22:28
 */
public interface RequirementWorkflow {

    WorkflowResult run(WorkflowRequest request);
}
