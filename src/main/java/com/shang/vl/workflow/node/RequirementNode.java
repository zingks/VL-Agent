package com.shang.vl.workflow.node;

import com.shang.vl.workflow.WorkflowState;
import com.shang.vl.workflow.agent.RequirementAgent;
import org.bsc.langgraph4j.state.AgentState;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RequirementNode extends AbstractWorkflowNode {

    private final RequirementAgent requirementAgent;

    public RequirementNode(final RequirementAgent requirementAgent) {
        this.requirementAgent = requirementAgent;
    }

    public Map<String, Object> execute(final AgentState state) {
        final WorkflowState workflowState = getWorkflowState(state);
        final String requirement = requirementAgent.generateRequirementFromImage(
                workflowState.getImageUrl(),
                workflowState.getOriginalDemand()
        );
        workflowState.setRequirement(requirement);
        return toStateUpdate(workflowState);
    }
}
