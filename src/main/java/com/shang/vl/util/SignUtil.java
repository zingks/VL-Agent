package com.shang.vl.util;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;

/**
 * Created by shangwei2009@hotmail.com on 2025/8/27 10:54
 */
public class SignUtil {

    private static final String signKey = "2c879c36c97a3ecf93496228a88938a8";

    public static String computeHash(String requestPayload) {
        final byte[] bytes = (requestPayload + signKey).getBytes(StandardCharsets.UTF_8);
        // 创建SM3 Digest实例
        Digest digest = new SM3Digest();
        // 初始化SM3算法
        digest.update(bytes, 0, bytes.length);

        // 创建输出的字节数组
        byte[] hash = new byte[digest.getDigestSize()];

        // 生成哈希值
        digest.doFinal(hash, 0);

        return Hex.toHexString(hash);
    }
}
