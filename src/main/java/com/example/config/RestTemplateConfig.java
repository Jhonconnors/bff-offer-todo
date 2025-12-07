package com.example.config;

import com.example.config.auth.TokenInterceptor;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    @Primary
    public RestTemplate restTemplate(TokenInterceptor tokenInterceptor) {
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(60))
                .setResponseTimeout(Timeout.ofSeconds(60))
                .setConnectionRequestTimeout(Timeout.ofSeconds(60))
                .build();

        PoolingHttpClientConnectionManager connManager =
                PoolingHttpClientConnectionManagerBuilder.create()
                        .setMaxConnTotal(200)
                        .setMaxConnPerRoute(50)
                        .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(config)
                .setConnectionManager(connManager)
                .evictIdleConnections(TimeValue.ofSeconds(15))
                .build();

        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory(httpClient);

        RestTemplate rt = new RestTemplate(factory);
        rt.getInterceptors().add(tokenInterceptor);

        return rt;
    }

    @Bean("simpleRestTemplate")
    public RestTemplate simpleRestTemplate() {
        return new RestTemplate();
    }
}
