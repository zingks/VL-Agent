package com.shang.vl.workflow.agent.impl;

import com.shang.vl.handler.ResponseHandler;
import com.shang.vl.prompt.PromptKeys;
import com.shang.vl.prompt.PromptManager;
import com.shang.vl.workflow.agent.RequirementAgent;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 基于 Prompt 模板的需求生成 Agent。
 * Created by shangwei2009@hotmail.com on 2026/3/6 22:25
 */
@Component
public class PromptRequirementAgent implements RequirementAgent {

    private final StreamingChatModel streamingVLModel;
    private final PromptManager promptManager;

    public PromptRequirementAgent(@Qualifier("streamingVLModel") final StreamingChatModel streamingVLModel,
                                  final PromptManager promptManager) {
        this.streamingVLModel = streamingVLModel;
        this.promptManager = promptManager;
    }

    @Override
    public String generateRequirementFromImage(final String imageUrl, final String originalDemand) {
        if (StringUtils.isBlank(imageUrl)) {
            throw new IllegalArgumentException("imageUrl cannot be blank");
        }

        final String prompt = promptManager.render(PromptKeys.IMAGE_TO_DEMAND, Map.of(
                "oriDemand", StringUtils.defaultString(originalDemand)
        ));

        final UserMessage userMessage = UserMessage.from(
                ImageContent.from(imageUrl, ImageContent.DetailLevel.HIGH),
                TextContent.from(prompt)
        );

        final ResponseHandler responseHandler = new ResponseHandler();
        streamingVLModel.chat(ChatRequest.builder()
                .messages(userMessage)
                .build(), responseHandler);

        return responseHandler.getResponseText();
    }
}
