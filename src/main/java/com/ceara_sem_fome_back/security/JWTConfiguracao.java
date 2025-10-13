package com.ceara_sem_fome_back.security;

import com.ceara_sem_fome_back.service.AdministradorService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class JWTConfiguracao {

    private final AdministradorService administradorService;
    private final PasswordEncoder passwordEncoder;

    public JWTConfiguracao(AdministradorService administradorService, PasswordEncoder passwordEncoder) {
        this.administradorService = administradorService;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(administradorService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthenticationManager authManager) throws Exception {

        AntPathRequestMatcher h2ConsoleMatcher = new AntPathRequestMatcher("/h2-console/**");
        AntPathRequestMatcher admLoginMatcher = new AntPathRequestMatcher("/adm/login");
        AntPathRequestMatcher loginPostMatcher = new AntPathRequestMatcher("/login", "POST");

        http
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.disable())) // necessário pro H2
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(h2ConsoleMatcher).permitAll()
                        .requestMatchers(admLoginMatcher).permitAll()
                        .requestMatchers(loginPostMatcher).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilter(new JWTAutenticarFilter(authManager))
                .addFilter(new JWTValidarFilter(authManager))
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfiguration() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        CorsConfiguration corsConfiguration = new CorsConfiguration().applyPermitDefaultValues();
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedOriginPattern("*");

        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }
}
