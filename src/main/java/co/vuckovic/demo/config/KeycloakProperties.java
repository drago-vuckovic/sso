package co.vuckovic.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties("keycloak")
public record KeycloakProperties(
        String serverUrl,
        String realm,
        String masterRealm,
        String masterClientId,
        String adminUser,
        String adminPassword
) {}