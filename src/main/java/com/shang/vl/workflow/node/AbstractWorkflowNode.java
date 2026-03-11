package com.shang.vl.workflow.node;

import com.shang.vl.workflow.WorkflowGraphKeys;
import com.shang.vl.workflow.WorkflowState;
import org.bsc.langgraph4j.state.AgentState;

import java.util.Map;

/**
 * 节点公共能力：
 * 统一读取/回写 WorkflowState，保证图中状态沿同一 key 在节点间传递。
 */
public abstract class AbstractWorkflowNode {

    protected WorkflowState getWorkflowState(final AgentState state) {
        return state.value(WorkflowGraphKeys.KEY_WORKFLOW_STATE, new WorkflowState());
    }

    protected Map<String, Object> toStateUpdate(final WorkflowState workflowState) {
        return Map.of(WorkflowGraphKeys.KEY_WORKFLOW_STATE, workflowState);
    }
}
