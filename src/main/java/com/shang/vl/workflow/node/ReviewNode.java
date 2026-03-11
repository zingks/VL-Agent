package com.shang.vl.workflow.node;

import com.shang.vl.workflow.WorkflowState;
import com.shang.vl.workflow.agent.TestCaseReviewAgent;
import org.bsc.langgraph4j.state.AgentState;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ReviewNode extends AbstractWorkflowNode {

    private final TestCaseReviewAgent testCaseReviewAgent;

    public ReviewNode(final TestCaseReviewAgent testCaseReviewAgent) {
        this.testCaseReviewAgent = testCaseReviewAgent;
    }

    public Map<String, Object> execute(final AgentState state) {
        final WorkflowState workflowState = getWorkflowState(state);
        testCaseReviewAgent.reviewTestCases(workflowState);
        return toStateUpdate(workflowState);
    }
}
