package com.shang.vl.workflow.agent.impl;

import com.shang.vl.handler.ResponseHandler;
import com.shang.vl.prompt.PromptKeys;
import com.shang.vl.prompt.PromptManager;
import com.shang.vl.workflow.WorkflowState;
import com.shang.vl.workflow.agent.TestCaseReviewAgent;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * 基于 Prompt 模板的测试用例评审 Agent。
 * Created by shangwei2009@hotmail.com on 2026/3/6 22:26
 */
@Component
public class PromptTestCaseReviewAgent implements TestCaseReviewAgent {

    private final StreamingChatModel streamingTextModel;
    private final PromptManager promptManager;

    public PromptTestCaseReviewAgent(@Qualifier("streamingTextModel") final StreamingChatModel streamingTextModel,
                                     final PromptManager promptManager) {
        this.streamingTextModel = streamingTextModel;
        this.promptManager = promptManager;
    }

    @Override
    public String reviewTestCases(final WorkflowState state) {
        if (state == null) {
            throw new IllegalArgumentException("state cannot be null");
        }

        final String testCases = StringUtils.defaultString(state.getTestCases());
        if (StringUtils.isBlank(testCases)) {
            throw new IllegalArgumentException("testCases cannot be blank");
        }

        final String systemPrompt = promptManager.loadTemplate(PromptKeys.TEST_CASES_REVIEW);
        final SystemMessage systemMessage = SystemMessage.from(systemPrompt);
        final UserMessage userMessage = UserMessage.from(TextContent.from(buildReviewInput(state)));

        final ResponseHandler responseHandler = new ResponseHandler();
        streamingTextModel.chat(ChatRequest.builder()
                .messages(systemMessage, userMessage)
                .build(), responseHandler);

        final String result = responseHandler.getResponseText();
        state.setReview(result);
        return result;
    }

    private String buildReviewInput(final WorkflowState state) {
        final String requirement = StringUtils.defaultString(state.getRequirement());
        final String testCases = StringUtils.defaultString(state.getTestCases());
        if (StringUtils.isBlank(requirement)) {
            return "测试用例如下：\n" + testCases;
        }
        return "测试用例如下：\n" + testCases;
    }
}
