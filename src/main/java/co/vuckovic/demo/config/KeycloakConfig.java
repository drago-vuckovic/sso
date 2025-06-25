package co.vuckovic.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.*;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import reactor.core.publisher.Mono;

import java.util.Collection;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class KeycloakConfig {

    @Bean
    public SecurityWebFilterChain securityFilterChain(
            ServerHttpSecurity http,
            ReactiveJwtAuthenticationConverterAdapter jwtConverter
    ) {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeExchange(ex -> ex
                        .pathMatchers("/api/users").hasRole("USER")
                        .pathMatchers("/api/edit").hasRole("EDITOR")
                        .pathMatchers("/api/admin/**")
                        .access(this::adminOrManageUsers)
                        .pathMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/swagger-ui/index.html",
                                "/v3/api-docs/**",
                                "/webjars/**"
                        ).permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(o -> o
                        .jwt(j -> j.jwtAuthenticationConverter(jwtConverter))
                )
                .build();
    }

    @Bean
    public ReactiveJwtAuthenticationConverterAdapter jwtConverter() {
        var grantedConverter = new JwtGrantedAuthoritiesConverter();
        grantedConverter.setAuthorityPrefix("ROLE_");
        grantedConverter.setAuthoritiesClaimName("roles");

        var conv = new JwtAuthenticationConverter();
        conv.setJwtGrantedAuthoritiesConverter(grantedConverter);
        return new ReactiveJwtAuthenticationConverterAdapter(conv);
    }

    private Mono<org.springframework.security.authorization.AuthorizationDecision>
    adminOrManageUsers(
            Mono<org.springframework.security.core.Authentication> authMono,
            AuthorizationContext ctx
    ) {
        return authMono.map(auth -> {
            Collection<? extends GrantedAuthority> auths = auth.getAuthorities();
            boolean ok = auths.stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(a -> a.equals("ROLE_ADMIN")
                            || a.equals("ROLE_manage-users"));
            return new org.springframework.security.authorization.AuthorizationDecision(ok);
        });
    }
}