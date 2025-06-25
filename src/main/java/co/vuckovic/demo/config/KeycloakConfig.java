package co.vuckovic.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class KeycloakConfig {

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http,
                                                      ReactiveJwtAuthenticationConverterAdapter jwtConverter) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/api/users").hasRole("USER")
                        .pathMatchers("/api/edit").hasRole("EDITOR")
                        .pathMatchers("/api/admin/**").access(adminOrManageUsers())
                        .pathMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/swagger-ui/index.html",
                                "/v3/api-docs/**",
                                "/webjars/**"
                        ).permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter))
                )
                .build();
    }

    @Bean
    public ReactiveJwtAuthenticationConverterAdapter jwtConverter() {
        JwtGrantedAuthoritiesConverter grantedConverter = new JwtGrantedAuthoritiesConverter();
        grantedConverter.setAuthorityPrefix("ROLE_");
        grantedConverter.setAuthoritiesClaimName("roles");

        JwtAuthenticationConverter jwtAuthConverter = new JwtAuthenticationConverter();
        jwtAuthConverter.setJwtGrantedAuthoritiesConverter(grantedConverter);

        return new ReactiveJwtAuthenticationConverterAdapter(jwtAuthConverter);
    }

    private ReactiveAuthorizationManager<AuthorizationContext> adminOrManageUsers() {
        return (authMono, context) ->
                authMono
                        .map(auth -> {
                            boolean allowed = auth.getAuthorities().stream()
                                    .map(GrantedAuthority::getAuthority)
                                    .anyMatch(a -> a.equals("ROLE_ADMIN") || a.equals("manage-users"));
                            return new AuthorizationDecision(allowed);
                        });
    }
}





//package co.vuckovic.demo.config;
//
//import org.keycloak.OAuth2Constants;
//import org.keycloak.admin.client.Keycloak;
//import org.keycloak.admin.client.KeycloakBuilder;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Configuration
//public class KeycloakConfig {
//
//    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
//    private String jwkSetUri;
//
//    @Value("${keycloak.server-url}")
//    private String serverUrl;
//
//    @Value("${keycloak.admin-user}")
//    private String adminUser;
//
//    @Value("${keycloak.admin-password}")
//    private String adminPassword;
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/api/users").hasRole("USER")
//                        .requestMatchers("/api/edit").hasRole("EDITOR")
//                        .requestMatchers("/api/admin/**")
//                        .access(new WebExpressionAuthorizationManager(
//                                "hasRole('ADMIN') or hasAuthority('ROLE_manage-users')"
//                        ))
//                        .anyRequest().authenticated()
//                )
//                .oauth2ResourceServer(oauth2 -> oauth2
//                        .jwt(jwt -> jwt
//                                .decoder(org.springframework.security.oauth2.jwt.NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build())
//                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
//                        )
//                );
//        return http.build();
//    }
//
//    @Bean
//    public JwtAuthenticationConverter jwtAuthenticationConverter() {
//        JwtAuthenticationConverter conv = new JwtAuthenticationConverter();
//        conv.setJwtGrantedAuthoritiesConverter(jwt -> {
//            List<String> roles = jwt.getClaimAsStringList("roles");
//            if (roles == null) return List.of();
//            return roles.stream()
//                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
//                    .collect(Collectors.toList());
//        });
//        return conv;
//    }
//
//    @Bean
//    public Keycloak keycloakAdmin() {
//        return KeycloakBuilder.builder()
//                .serverUrl(serverUrl)
//                .realm("master")
//                .grantType(OAuth2Constants.PASSWORD)
//                .clientId("admin-cli")
//                .username(adminUser)
//                .password(adminPassword)
//                .build();
//    }
//}
