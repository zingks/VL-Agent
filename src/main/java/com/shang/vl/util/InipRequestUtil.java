package com.shang.vl.util;

import com.shang.vl.model.InipResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.LinkedHashMap;

/**
 * Created by shangwei2009@hotmail.com on 2025/8/27 19:07
 */
public class InipRequestUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmmss");
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String HASH_HEADER = "X-Authorization";

    public static InipResponse uploadImage(WebClient webClient, byte[] imageBytes) {
        final LocalDateTime now = LocalDateTime.now();
        final String date = DATE_FORMATTER.format(now);
        final String time = TIME_FORMATTER.format(now);

        final LinkedHashMap<String, Object> msgHead = new LinkedHashMap<>();
        msgHead.put("versionID", "1"); // 版本号
        msgHead.put("charSet", "UTF-8"); // 字符编码
        msgHead.put("orgSenderID", "0000000229"); // 请求方系统代码 与 私钥成对
        msgHead.put("orgReceiverID", "0000000229"); // 请求方系统代码 与 私钥成对
        msgHead.put("orgSendDate", date); // 请求方日期
        msgHead.put("orgSendTime", time); // 请求方时间
        msgHead.put("orgSendTransID", String.format("%s%s%06d", date, time, RANDOM.nextInt(1000000))); // 请求流水号

        final LinkedHashMap<String, Object> msgBody = new LinkedHashMap<>();
        final String base64 = Base64.getEncoder().encodeToString(imageBytes);
        msgBody.put("fileContent", base64); // base64图片
        msgBody.put("expireTime", 120); // 过期时间（秒），默认1个月

        final LinkedHashMap<String, Object> request = new LinkedHashMap<>();
        request.put("msgHead", msgHead);
        request.put("msgBody", msgBody);

        final String requestPayload = JacksonUtil.writeValueAsString(request);
        final String hash = SignUtil.computeHash(requestPayload);

        final String result = webClient.post()
                .uri("/api/inip-bos/v1/object/put")
                .header(HASH_HEADER, "SIGN-TYPE=SM3;SIGN=" + hash)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        final InipResponse inipResponse = JacksonUtil.parseObject(result, InipResponse.class);
        if (StringUtils.isBlank(inipResponse.getMsgBody().getObjectKey())) {
            throw new RuntimeException("上传图片失败");
        }
        return inipResponse;

    }
}
