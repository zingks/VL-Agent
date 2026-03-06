package com.shang.vl.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Prompt 模板路径配置。
 * Created by shangwei2009@hotmail.com on 2026/3/6 22:10
 */
@Data
@Component
@ConfigurationProperties(prefix = "vl.prompt")
public class PromptProperties {

    private Map<String, String> templates = new LinkedHashMap<>();
}
