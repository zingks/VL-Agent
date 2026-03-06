package com.shang.vl.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Created by shangwei2009@hotmail.com on 2025/8/22 17:46
 */
@Configuration
public class WebClientConfig {

    @Bean(name = "inipWebClient")
    public WebClient inipWebClient() {
        return WebClient.builder()
                .baseUrl("http://10.11.99.0:19360")
                .defaultHeader("X-API-Mode", "AI")
                .defaultHeader("X-OrgSender-ID", "0000000229")
                .build();
    }



}
