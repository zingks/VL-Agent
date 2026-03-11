package com.shang.vl.workflow.node;

import com.shang.vl.workflow.WorkflowState;
import com.shang.vl.workflow.agent.TestCaseAgent;
import org.bsc.langgraph4j.state.AgentState;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TestCaseNode extends AbstractWorkflowNode {

    private final TestCaseAgent testCaseAgent;

    public TestCaseNode(final TestCaseAgent testCaseAgent) {
        this.testCaseAgent = testCaseAgent;
    }

    public Map<String, Object> execute(final AgentState state) {
        final WorkflowState workflowState = getWorkflowState(state);
        testCaseAgent.generateTestCases(workflowState);
        return toStateUpdate(workflowState);
    }
}
