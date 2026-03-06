package com.shang.vl.workflow.agent.impl;

import com.shang.vl.handler.ResponseHandler;
import com.shang.vl.prompt.PromptKeys;
import com.shang.vl.prompt.PromptManager;
import com.shang.vl.workflow.agent.TestCaseReviewAgent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;

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
    public String reviewTestCases(final String testCases) {
        if (StringUtils.isBlank(testCases)) {
            throw new IllegalArgumentException("testCases cannot be blank");
        }

        final String prompt = promptManager.render(PromptKeys.TEST_CASES_REVIEW, Map.of(
                "testCases", testCases
        ));
        final UserMessage userMessage = UserMessage.from(TextContent.from(prompt));

        final ResponseHandler responseHandler = new ResponseHandler();
        streamingTextModel.chat(ChatRequest.builder()
                .messages(userMessage)
                .build(), responseHandler);

        return responseHandler.getResponseText();
    }
}
