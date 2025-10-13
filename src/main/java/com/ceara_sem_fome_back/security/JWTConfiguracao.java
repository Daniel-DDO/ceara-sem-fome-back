package com.ceara_sem_fome_back.security;

import com.ceara_sem_fome_back.service.AdministradorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .authorizeHttpRequests(auth -> auth
                        //libera H2 console
                        .requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll()
                        //libera endpoints públicos
                        .requestMatchers(new AntPathRequestMatcher("/health")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/version")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/adm/login", "POST")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/login", "POST")).permitAll()
                        //libera tudo (é pra ser temporário, dps remover se quiser autenticação)
                        .anyRequest().permitAll()
                )
                .addFilter(new JWTAutenticarFilter(authManager, tokenSenha)) //autenticação
                .addFilter(new JWTValidarFilter(authManager, tokenSenha)) //validação
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfiguration() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration cors = new CorsConfiguration().applyPermitDefaultValues();
        cors.addAllowedMethod("*");
        cors.addAllowedHeader("*");
        cors.addAllowedOriginPattern("*");
        source.registerCorsConfiguration("/**", cors);
        return source;
    }
}
