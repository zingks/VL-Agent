package com.shang.vl.service;

import com.shang.vl.model.InipResponse;
import com.shang.vl.util.InipRequestUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 图片上传与外链转换。
 * Created by shangwei2009@hotmail.com on 2026/3/6 22:18
 */
@Service
public class ImageUploadService {

    private final WebClient inipWebClient;

    public ImageUploadService(@Qualifier("inipWebClient") final WebClient inipWebClient) {
        this.inipWebClient = inipWebClient;
    }

    public String uploadAndGetImageUrl(final Path imagePath) throws IOException {
        final byte[] imageBytes = Files.readAllBytes(imagePath);
        return uploadAndGetImageUrl(imageBytes);
    }

    public String uploadAndGetImageUrl(final byte[] imageBytes) {
        final InipResponse inipResponse = InipRequestUtil.uploadImage(inipWebClient, imageBytes);
        final String objectKey = inipResponse.getMsgBody().getObjectKey();
        return "http://images.chinaums.com?fileId=%s".formatted(objectKey);
    }
}
