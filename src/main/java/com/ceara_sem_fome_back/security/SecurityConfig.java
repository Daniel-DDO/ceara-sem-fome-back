package com.ceara_sem_fome_back.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                
                // LIBERA POST: Rota para iniciar a recuperação (CPF/Email check)
                .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/auth/recuperar")).permitAll() 
                
                // LIBERA POST: Rota FINAL para redefinir a senha
                .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/auth/resetar-senha-final")).permitAll() 
                
                // Permite GET para o link de validação do token (pedágio)
                .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/auth/reset-password")).permitAll()

                // Permite TODAS as rotas de teste (GET/POST)
                .requestMatchers(AntPathRequestMatcher.antMatcher("/test/**")).permitAll()
                
                // Rotas essenciais do projeto
                .requestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/adm/login")).permitAll()
                
                // Bloqueia todas as outras rotas (exige autenticação)
                .anyRequest().authenticated()
            )
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));

        return http.build();
    }
}
