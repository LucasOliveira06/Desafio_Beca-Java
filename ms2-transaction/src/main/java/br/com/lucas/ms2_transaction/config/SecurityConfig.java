package br.com.lucas.ms2_transaction.config;

import br.com.lucas.ms2_transaction.adapter.security.Ms1AuthenticationProvider;
import br.com.lucas.ms2_transaction.adapter.security.Ms1AuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            Ms1AuthenticationProvider ms1AuthenticationProvider,
            Ms1AuthenticationEntryPoint ms1AuthenticationEntryPoint
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                .authenticationProvider(ms1AuthenticationProvider)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/actuator/health"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(httpBasic -> httpBasic
                        .authenticationEntryPoint(ms1AuthenticationEntryPoint)
                );

        return http.build();
    }
}
