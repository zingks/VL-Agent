package com.shang.vl.workflow;

import com.shang.vl.workflow.agent.RequirementAgent;
import com.shang.vl.workflow.agent.TestCaseAgent;
import com.shang.vl.workflow.agent.TestCaseReviewAgent;
import org.apache.commons.lang3.StringUtils;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.action.AsyncEdgeAction;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.state.AgentState;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

    private static final String KEY_IMAGE_URL = "imageUrl";
    private static final String KEY_ORIGINAL_DEMAND = "originalDemand";
    private static final String KEY_MODE = "mode";
    private static final String KEY_REQUIREMENT = "requirement";
    private static final String KEY_TEST_CASES = "testCases";
    private static final String KEY_REVIEW = "review";

    private final RequirementAgent requirementAgent;
    private final TestCaseAgent testCaseAgent;
    private final TestCaseReviewAgent testCaseReviewAgent;
    private final CompiledGraph<AgentState> compiledGraph;

    public SequentialRequirementWorkflow(final RequirementAgent requirementAgent,
                                         final TestCaseAgent testCaseAgent,
                                         final TestCaseReviewAgent testCaseReviewAgent) {
        this.requirementAgent = requirementAgent;
        this.testCaseAgent = testCaseAgent;
        this.testCaseReviewAgent = testCaseReviewAgent;
        this.compiledGraph = buildGraph();
    }

    @Override
    public WorkflowResult run(final WorkflowRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request cannot be null");
        }
        if (StringUtils.isBlank(request.getImageUrl())) {
            throw new IllegalArgumentException("imageUrl cannot be blank");
        }

        final WorkflowMode mode = request.getMode() == null ? WorkflowMode.FULL_PIPELINE : request.getMode();
        final Map<String, Object> inputState = new HashMap<>();
        inputState.put(KEY_IMAGE_URL, request.getImageUrl());
        inputState.put(KEY_ORIGINAL_DEMAND, StringUtils.defaultString(request.getOriginalDemand()));
        inputState.put(KEY_MODE, mode.name());

        final AgentState finalState = compiledGraph.invoke(inputState)
                .orElseThrow(() -> new IllegalStateException("LangGraph invoke returned empty state"));

        final String modeName = finalState.value(KEY_MODE, WorkflowMode.FULL_PIPELINE.name());
        final WorkflowMode finalMode = WorkflowMode.valueOf(modeName);
        final Optional<String> requirement = finalState.value(KEY_REQUIREMENT);
        final Optional<String> testCases = finalState.value(KEY_TEST_CASES);
        final Optional<String> review = finalState.value(KEY_REVIEW);

        return new WorkflowResult()
                .setMode(finalMode)
                .setRequirement(requirement.orElse(null))
                .setTestCases(testCases.orElse(null))
                .setReview(review.orElse(null));
    }

    private CompiledGraph<AgentState> buildGraph() {
        try {
            final StateGraph<AgentState> graph = new StateGraph<>(AgentState::new);

            graph.addNode(NODE_REQUIREMENT, AsyncNodeAction.node_async(state -> {
                final String imageUrl = state.value(KEY_IMAGE_URL, "");
                final String originalDemand = state.value(KEY_ORIGINAL_DEMAND, "");
                final String requirement = requirementAgent.generateRequirementFromImage(imageUrl, originalDemand);
                return Map.of(KEY_REQUIREMENT, requirement);
            }));

            graph.addNode(NODE_TEST_CASES, AsyncNodeAction.node_async(state -> {
                final String requirement = state.value(KEY_REQUIREMENT, "");
                final String testCases = testCaseAgent.generateTestCases(requirement);
                return Map.of(KEY_TEST_CASES, testCases);
            }));

            graph.addNode(NODE_REVIEW, AsyncNodeAction.node_async(state -> {
                final String testCases = state.value(KEY_TEST_CASES, "");
                final String review = testCaseReviewAgent.reviewTestCases(testCases);
                return Map.of(KEY_REVIEW, review);
            }));

            graph.addEdge(StateGraph.START, NODE_REQUIREMENT);

            graph.addConditionalEdges(
                    NODE_REQUIREMENT,
                    AsyncEdgeAction.edge_async(state ->
                            WorkflowMode.REQUIREMENT_ONLY.name().equals(state.value(KEY_MODE, WorkflowMode.FULL_PIPELINE.name()))
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
                            WorkflowMode.REQUIREMENT_AND_TEST_CASES.name().equals(state.value(KEY_MODE, WorkflowMode.FULL_PIPELINE.name()))
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
