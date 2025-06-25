package co.vuckovic.demo.config;

import co.vuckovic.demo.service.KeycloakTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    private final KeycloakProperties props;
    private final KeycloakTokenProvider tokenProvider;

    @Bean
    public WebClient keycloakWebClient(WebClient.Builder builder) {
        ExchangeFilterFunction authFilter = ExchangeFilterFunction.ofRequestProcessor(req ->
                tokenProvider.getToken()
                        .map(t -> ClientRequest.from(req)
                                .headers(h -> h.setBearerAuth(t))
                                .build())
        );

        return builder
                .baseUrl(props.serverUrl())
                .filter(authFilter)
                .build();
    }
}