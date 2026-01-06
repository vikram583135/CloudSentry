package com.devops.platform.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * WebClient Configuration
 * 
 * Configures WebClient for external API calls (Slack, PagerDuty, OpenAI, etc.)
 */
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofSeconds(10))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(10, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(10, TimeUnit.SECONDS)));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient));
    }

    @Bean
    public WebClient slackWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("https://hooks.slack.com")
                .build();
    }

    @Bean
    public WebClient pagerDutyWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("https://events.pagerduty.com/v2")
                .build();
    }

    @Bean
    public WebClient openAiWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("https://api.openai.com/v1")
                .build();
    }
}
