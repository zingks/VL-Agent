package com.shang.vl.workflow.agent;

import com.shang.vl.workflow.WorkflowState;

/**
 * 测试用例生成 Agent。
 * Created by shangwei2009@hotmail.com on 2026/3/6 22:24
 */
public interface TestCaseAgent {

    String generateTestCases(WorkflowState state);
}
