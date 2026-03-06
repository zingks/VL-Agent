package com.shang.vl.config;

import dev.langchain4j.http.client.jdk.JdkHttpClientBuilder;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;

/**
 * Created by shangwei2009@hotmail.com on 2025/8/27 10:50
 */
@Configuration
public class VLMConfig {

    private static final String ORG_SENDER_ID = "0000000229";
    private static final String API_MODE = "AI";

    private static final String VLM_BASE_URL = "http://10.11.121.95:8084/v1";
    private static final String VLM_API_KEY = "sk-CswNAuIfHahuACXUFe85B162E25f47D693C3Bc085cE20fFd";

    private static final String MODEL_VL = "Qwen/Qwen3-VL-32B-Instruct";
    private static final String MODEL_TEXT = "Qwen/Qwen3-32B";

    @Bean(name = "vlModel")
    public ChatModel vlModel() {
        final JdkHttpClientBuilder jdkHttpClientBuilder = new Qwen3JdkHttpClientBuilder().httpClientBuilder(HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1));
        return OpenAiChatModel.builder()
                .baseUrl(VLM_BASE_URL)
                .apiKey(VLM_API_KEY)
                .customHeaders(Map.of("X-API-Mode", API_MODE, "X-OrgSender-ID", ORG_SENDER_ID))
                .httpClientBuilder(jdkHttpClientBuilder)
                .logRequests(true)
                .logResponses(true)
                .modelName(MODEL_VL)
                .temperature(0.001)
                .timeout(Duration.ofSeconds(600))
                .strictJsonSchema(false)
                .strictTools(false)
                .build();
    }

    @Bean(name = "streamingVLModel")
    public StreamingChatModel streamingVLModel() {
        return createStreamingModel(MODEL_VL);
    }

    /**
     * 文本模型：用于需求 -> 测试用例、测试用例评审。
     */
    @Bean(name = "streamingTextModel")
    public StreamingChatModel streamingTextModel() {
        return createStreamingModel(MODEL_TEXT);
    }

    private StreamingChatModel createStreamingModel(final String modelName) {
        final JdkHttpClientBuilder jdkHttpClientBuilder = new Qwen3JdkHttpClientBuilder().httpClientBuilder(HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1));
        return OpenAiStreamingChatModel.builder()
                .baseUrl(VLM_BASE_URL)
                .apiKey(VLM_API_KEY)
                .customHeaders(Map.of("X-API-Mode", API_MODE, "X-OrgSender-ID", ORG_SENDER_ID))
                .httpClientBuilder(jdkHttpClientBuilder)
                .logRequests(true)
                .logResponses(true)
                .modelName(modelName)
                .temperature(0.001)
                .timeout(Duration.ofSeconds(600))
                .strictJsonSchema(false)
                .strictTools(false)
                .build();
    }

}
