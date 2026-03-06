package com.shang.vl.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shang.vl.util.JacksonUtil;
import dev.langchain4j.exception.HttpException;
import dev.langchain4j.http.client.HttpRequest;
import dev.langchain4j.http.client.SuccessfulHttpResponse;
import dev.langchain4j.http.client.jdk.JdkHttpClient;
import dev.langchain4j.http.client.jdk.JdkHttpClientBuilder;
import dev.langchain4j.http.client.sse.ServerSentEventListener;
import dev.langchain4j.http.client.sse.ServerSentEventParser;

import java.util.HashMap;
import java.util.Map;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;

/**
 * Qwen3不要深度思考
 * <p>
 * Created by shangwei2009@hotmail.com on 2025/5/26 19:44
 */
public class Qwen3JdkHttpClientBuilder extends JdkHttpClientBuilder {

    @Override
    public JdkHttpClient build() {
        return new Qwen3JdkHttpClient(this);
    }
}

class Qwen3JdkHttpClient extends JdkHttpClient {

    static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .enable(INDENT_OUTPUT)
            .disable(FAIL_ON_IGNORED_PROPERTIES);

    static final TypeReference<HashMap<String, Object>> TYPE_REFERENCE = new TypeReference<>() {
    };

    public Qwen3JdkHttpClient(final JdkHttpClientBuilder builder) {
        super(builder);
    }

    @Override
    public SuccessfulHttpResponse execute(final HttpRequest request) throws HttpException {
        final HttpRequest modified = modifyBody(request);
        return super.execute(modified);
    }

    @Override
    public void execute(final HttpRequest request, final ServerSentEventParser parser, final ServerSentEventListener listener) {
        final HttpRequest modified = modifyBody(request);
        super.execute(modified, parser, listener);
    }

    private static HttpRequest modifyBody(final HttpRequest request) {
        final HashMap<String, Object> map = JacksonUtil.parse(OBJECT_MAPPER, request.body(), TYPE_REFERENCE);
        map.put("chat_template_kwargs", Map.of("enable_thinking", false));
        return HttpRequest.builder().method(request.method())
                .url(request.url())
                .headers(request.headers())
                .body(JacksonUtil.writeValueAsString(OBJECT_MAPPER, map))
                .build();
    }

}
