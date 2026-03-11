package com.shang.vl.workflow.agent.impl;

import com.shang.vl.handler.ResponseHandler;
import com.shang.vl.prompt.PromptKeys;
import com.shang.vl.prompt.PromptManager;
import com.shang.vl.workflow.agent.RequirementAgent;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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
    public String generateRequirementFromImages(final List<String> imageUrls, final String originalDemand) {
        if (imageUrls == null || imageUrls.stream().allMatch(StringUtils::isBlank)) {
            throw new IllegalArgumentException("imageUrls cannot be empty");
        }

        final String systemPrompt = promptManager.loadTemplate(PromptKeys.IMAGE_TO_DEMAND);
        final String userInput = buildUserInput(originalDemand);
        final List<Content> contents = new ArrayList<>(imageUrls.size() + 1);
        for (final String imageUrl : imageUrls) {
            if (StringUtils.isBlank(imageUrl)) {
                continue;
            }
            contents.add(ImageContent.from(imageUrl, ImageContent.DetailLevel.HIGH));
        }
        contents.add(TextContent.from(userInput));
        final UserMessage userMessage = UserMessage.from(contents);

        final ResponseHandler responseHandler = new ResponseHandler();
        streamingVLModel.chat(ChatRequest.builder()
                .messages(SystemMessage.from(systemPrompt), userMessage)
                .build(), responseHandler);

        return responseHandler.getResponseText();
    }

    private String buildUserInput(final String originalDemand) {
        if (StringUtils.isBlank(originalDemand)) {
            return "请结合图片内容提取并输出结构化需求文档。";
        }
        return "请结合图片内容与以下原始需求，输出结构化需求文档：\n" + originalDemand;
    }
}
