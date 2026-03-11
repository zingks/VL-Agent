package com.shang.vl.workflow;

import com.shang.vl.workflow.node.RequirementNode;
import com.shang.vl.workflow.node.ReviewNode;
import com.shang.vl.workflow.node.TestCaseNode;
import org.apache.commons.lang3.StringUtils;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.action.AsyncEdgeAction;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.state.AgentState;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * LangGraph 节点图编排：
 * 需求生成 -> 测试用例生成 -> 测试用例评审。
 * Created by sawang2@chinaums.com on 2026/3/6 22:29
 */
@Service
public class SequentialRequirementWorkflow implements RequirementWorkflow {

    private static final String NODE_REQUIREMENT = "requirement";
    private static final String NODE_TEST_CASES = "test_cases";
    private static final String NODE_REVIEW = "review";

    private final RequirementNode requirementNode;
    private final TestCaseNode testCaseNode;
    private final ReviewNode reviewNode;
    private final CompiledGraph<AgentState> compiledGraph;

    public SequentialRequirementWorkflow(final RequirementNode requirementNode,
                                         final TestCaseNode testCaseNode,
                                         final ReviewNode reviewNode) {
        this.requirementNode = requirementNode;
        this.testCaseNode = testCaseNode;
        this.reviewNode = reviewNode;
        this.compiledGraph = buildGraph();
    }

    @Override
    public WorkflowResult run(final WorkflowRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request cannot be null");
        }
        if (request.getImageUrls() == null || request.getImageUrls().stream().allMatch(StringUtils::isBlank)) {
            throw new IllegalArgumentException("imageUrls cannot be empty");
        }

        final WorkflowMode mode = request.getMode() == null ? WorkflowMode.FULL_PIPELINE : request.getMode();
        final WorkflowState workflowState = new WorkflowState()
                .setImageUrls(request.getImageUrls())
                .setOriginalDemand(StringUtils.defaultString(request.getOriginalDemand()));

        // WorkflowState 作为单一对象挂在 graph state 中，后续每个节点都在同一对象上读写字段并回传。
        final Map<String, Object> inputState = new HashMap<>();
        inputState.put(WorkflowGraphKeys.KEY_WORKFLOW_STATE, workflowState);
        inputState.put(WorkflowGraphKeys.KEY_MODE, mode.name());

        final AgentState finalState = compiledGraph.invoke(inputState)
                .orElseThrow(() -> new IllegalStateException("LangGraph invoke returned empty state"));

        final String modeName = finalState.value(WorkflowGraphKeys.KEY_MODE, WorkflowMode.FULL_PIPELINE.name());
        final WorkflowMode finalMode = WorkflowMode.valueOf(modeName);
        final WorkflowState finalWorkflowState = finalState.value(WorkflowGraphKeys.KEY_WORKFLOW_STATE, new WorkflowState());

        return new WorkflowResult()
                .setMode(finalMode)
                .setRequirement(finalWorkflowState.getRequirement())
                .setTestCases(finalWorkflowState.getTestCases())
                .setReview(finalWorkflowState.getReview());
    }

    private CompiledGraph<AgentState> buildGraph() {
        try {
            final StateGraph<AgentState> graph = new StateGraph<>(AgentState::new);

            // 编排层只描述拓扑结构；节点业务逻辑下沉到独立 Node 类，便于单测与后续扩展。
            graph.addNode(NODE_REQUIREMENT, AsyncNodeAction.node_async(requirementNode::execute));
            graph.addNode(NODE_TEST_CASES, AsyncNodeAction.node_async(testCaseNode::execute));
            graph.addNode(NODE_REVIEW, AsyncNodeAction.node_async(reviewNode::execute));

            graph.addEdge(StateGraph.START, NODE_REQUIREMENT);

            graph.addConditionalEdges(
                    NODE_REQUIREMENT,
                    AsyncEdgeAction.edge_async(state ->
                            WorkflowMode.REQUIREMENT_ONLY.name().equals(state.value(WorkflowGraphKeys.KEY_MODE, WorkflowMode.FULL_PIPELINE.name()))
                                    ? "end"
                                    : "to_test_cases"),
                    Map.of(
                            "end", StateGraph.END,
                            "to_test_cases", NODE_TEST_CASES
                    )
            );

            graph.addConditionalEdges(
                    NODE_TEST_CASES,
                    AsyncEdgeAction.edge_async(state ->
                            WorkflowMode.REQUIREMENT_AND_TEST_CASES.name().equals(state.value(WorkflowGraphKeys.KEY_MODE, WorkflowMode.FULL_PIPELINE.name()))
                                    ? "end"
                                    : "to_review"),
                    Map.of(
                            "end", StateGraph.END,
                            "to_review", NODE_REVIEW
                    )
            );

            graph.addEdge(NODE_REVIEW, StateGraph.END);
            return graph.compile();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build LangGraph workflow", e);
        }
    }
}
