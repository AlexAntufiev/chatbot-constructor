package chat.tamtam.bot.configuration.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestConfiguration {

    @Value("${rest.timeout.connect}")
    private int connectTimeout;

    @Value("${rest.timeout.request}")
    private int requestTimeout;

    @Bean
    public RestTemplate restTemplate(HttpComponentsClientHttpRequestFactory requestFactory) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(requestFactory);
        return restTemplate;
    }

    @Bean
    public HttpComponentsClientHttpRequestFactory requestFactory() {
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(requestTimeout);
        return requestFactory;
    }
}
