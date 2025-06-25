package co.vuckovic.demo.service;

import co.vuckovic.demo.config.KeycloakProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class KeycloakTokenProvider {

    private final KeycloakProperties props;
    private final WebClient.Builder webClientBuilder;
    private volatile Mono<String> cachedToken;

    public Mono<String> getToken() {
        if (cachedToken == null) {
            synchronized (this) {
                if (cachedToken == null) {
                    cachedToken = fetchNewToken()
                            .cache(Duration.ofMinutes(5));
                }
            }
        }
        return cachedToken;
    }

    private Mono<String> fetchNewToken() {
        var form = BodyInserters.fromFormData("grant_type", "password")
                .with("client_id", props.masterClientId())
                .with("username", props.adminUser())
                .with("password", props.adminPassword());

        return webClientBuilder
                .baseUrl(props.serverUrl())
                .build()
                .post()
                .uri("/realms/{realm}/protocol/openid-connect/token", props.masterRealm())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .bodyToMono(TokenResponse.class)
                .map(TokenResponse::access_token);
    }



    private static record TokenResponse(
            String access_token,
            String token_type,
            long expires_in
    ) {}
}
