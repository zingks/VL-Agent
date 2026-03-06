package com.shang.vl.handler;

import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;

import java.util.concurrent.CountDownLatch;

/**
 * Created by shangwei2009@hotmail.com on 2025/9/2 16:12
 */
public class ResponseHandler implements StreamingChatResponseHandler {

    private final CountDownLatch latch = new CountDownLatch(1);
    private String responseText;

    @Override
    public void onPartialResponse(final String partialResponse) {
        System.out.print(partialResponse);
    }

    @Override
    public void onCompleteResponse(final ChatResponse completeResponse) {
        latch.countDown();
        responseText = completeResponse.aiMessage().text();
    }

    @Override
    public void onError(final Throwable error) {
        latch.countDown();
        responseText = error.getMessage();
    }

    public String getResponseText() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return responseText;
    }
}
