package com.shang.vl.workflow.agent.impl;

import com.shang.vl.handler.ResponseHandler;
import com.shang.vl.prompt.PromptKeys;
import com.shang.vl.prompt.PromptManager;
import com.shang.vl.workflow.agent.TestCaseAgent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 基于 Prompt 模板的测试用例生成 Agent。
 * Created by shangwei2009@hotmail.com on 2026/3/6 22:25
 */
@Component
public class PromptTestCaseAgent implements TestCaseAgent {

    private final StreamingChatModel streamingTextModel;
    private final PromptManager promptManager;

    public PromptTestCaseAgent(@Qualifier("streamingTextModel") final StreamingChatModel streamingTextModel,
                               final PromptManager promptManager) {
        this.streamingTextModel = streamingTextModel;
        this.promptManager = promptManager;
    }

    @Override
    public String generateTestCases(final String requirement) {
        if (StringUtils.isBlank(requirement)) {
            throw new IllegalArgumentException("requirement cannot be blank");
        }

        final String prompt = promptManager.render(PromptKeys.REQUIREMENT_TO_TEST_CASES, Map.of(
                "requirement", requirement
        ));
        final UserMessage userMessage = UserMessage.from(TextContent.from(prompt));

        final ResponseHandler responseHandler = new ResponseHandler();
        streamingTextModel.chat(ChatRequest.builder()
                .messages(userMessage)
                .build(), responseHandler);

        return responseHandler.getResponseText();
    }
}
