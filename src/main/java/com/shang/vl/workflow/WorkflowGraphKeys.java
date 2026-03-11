package com.shang.vl.workflow;

/**
 * Workflow 图状态键常量，避免节点与编排层出现硬编码不一致。
 */
public final class WorkflowGraphKeys {

    public static final String KEY_WORKFLOW_STATE = "workflowState";
    public static final String KEY_MODE = "mode";

    private WorkflowGraphKeys() {
        throw new IllegalStateException("Utility class");
    }
}
