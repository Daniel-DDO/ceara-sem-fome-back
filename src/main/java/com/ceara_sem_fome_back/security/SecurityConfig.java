package com.ceara_sem_fome_back.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(new AntPathRequestMatcher("/adm/login")).permitAll() //libera o login de adm
                        .requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll()
                        .anyRequest().authenticated() //os outros endpoints precisam autenticar
                )
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .formLogin(form -> form.disable());

        return http.build();
    }
}
