package com.ceara_sem_fome_back.security;

import com.ceara_sem_fome_back.service.AdministradorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class JWTConfiguracao {

    private final AdministradorService administradorService;

    public JWTConfiguracao(AdministradorService administradorService) {
        this.administradorService = administradorService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(administradorService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthenticationManager authManager,
                                                   @Value("${api.guid.token.senha}") String tokenSenha) throws Exception {

        http
            .cors(cors -> cors.configurationSource(corsConfiguration()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilter(new JWTAutenticarFilter(authManager, tokenSenha))
            .addFilter(new JWTValidarFilter(authManager, tokenSenha))
            .authorizeHttpRequests(auth -> auth
                // Usando AntPathRequestMatcher explícito para resolver ambiguidade
                .requestMatchers(new AntPathRequestMatcher("/beneficiario/iniciar-cadastro", "POST")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/beneficiario/login", "POST")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/adm/login", "POST")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/auth/iniciar-recuperacao", "POST")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/auth/redefinir-senha-final", "POST")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/token/confirmar-cadastro", "GET")).permitAll()
                // [CORRIGIDO] Adicionamos a permissão para a rota de validação de token que estava em falta
                .requestMatchers(new AntPathRequestMatcher("/auth/validar-token-recuperacao", "GET")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/auth/redefinir-senha-pagina", "GET")).permitAll() // Mantemos esta por segurança
                .anyRequest().authenticated()
                
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfiguration() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(List.of("*"));
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("*"));
        source.registerCorsConfiguration("/**", cors);
        return source;
    }
}

