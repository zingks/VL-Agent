package com.shang.vl.prompt;

import com.shang.vl.config.PromptProperties;
import dev.langchain4j.model.input.PromptTemplate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Prompt 模板加载与渲染。
 * Created by shangwei2009@hotmail.com on 2026/3/6 22:13
 */
@Component
public class PromptManager {

    private final PromptProperties promptProperties;
    private final ResourceLoader resourceLoader;
    private final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();

    public PromptManager(final PromptProperties promptProperties, final ResourceLoader resourceLoader) {
        this.promptProperties = promptProperties;
        this.resourceLoader = resourceLoader;
    }

    public String render(final String templateKey, final Map<String, Object> variables) {
        final String template = loadTemplate(templateKey);
        final Map<String, Object> safeVariables = variables == null ? Collections.emptyMap() : variables;
        if (safeVariables.isEmpty()) {
            return template;
        }
        return PromptTemplate.from(template).apply(safeVariables).text();
    }

    public String loadTemplate(final String templateKey) {
        if (StringUtils.isBlank(templateKey)) {
            throw new IllegalArgumentException("templateKey cannot be blank");
        }
        return cache.computeIfAbsent(templateKey, this::readTemplate);
    }

    public void clearCache() {
        cache.clear();
    }

    private String readTemplate(final String templateKey) {
        final String templatePath = promptProperties.getTemplates().get(templateKey);
        if (StringUtils.isBlank(templatePath)) {
            throw new IllegalArgumentException("No prompt template path configured for key: " + templateKey);
        }

        final String location = templatePath.startsWith("classpath:") ? templatePath : "classpath:" + templatePath;
        final Resource resource = resourceLoader.getResource(location);
        if (!resource.exists()) {
            throw new IllegalStateException("Prompt template not found: " + location);
        }

        try (InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read prompt template: " + location, e);
        }
    }
}
