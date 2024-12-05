package com.community.api.configuration;

import io.netty.handler.codec.http.HttpObjectAggregator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ClientRequest;
import reactor.netty.http.client.HttpClient;
import io.netty.handler.codec.http.HttpHeaderNames;
import reactor.netty.resources.LoopResources;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMinutes(5)) // Optional: increase timeout
                .doOnRequest((req, conn) -> conn.addHandlerLast(new HttpObjectAggregator(10000000 * 1000000))) // Increase buffer size (1 MB)
                .wiretap(true);

        return WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient));
    }
}