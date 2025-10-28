package com.ceara_sem_fome_back.security;

import com.ceara_sem_fome_back.security.Handler.LoggingLogoutSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
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

    @Value("${api.guid.token.senha}")
    private String tokenSenha;

    //INJETAR O NOVO HANDLER
    @Autowired
    private LoggingLogoutSuccessHandler loggingLogoutSuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                 AuthenticationManager authManager) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfiguration()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilter(new JWTAutenticarFilter(authManager, tokenSenha))
                .addFilter(new JWTValidarFilter(authManager, tokenSenha))
                
                //CONFIGURAÇÃO DE LOGOUT
                .logout(logout -> logout
                    .logoutUrl("/auth/logout") //Define a URL de logout
                    .logoutSuccessHandler(loggingLogoutSuccessHandler) //Usa nosso handler de log
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID") //
                )
                //FIM DA CONFIGURAÇÃO DE LOGOUT
                
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                new AntPathRequestMatcher("/*/iniciar-cadastro"),
                                new AntPathRequestMatcher("/*/login"),
                                new AntPathRequestMatcher("/token/confirmar-cadastro"),
                                new AntPathRequestMatcher("/**/all"),
                                new AntPathRequestMatcher("/version"),
                                new AntPathRequestMatcher("/health"),
                                new AntPathRequestMatcher("/**/meu-perfil"),
                                new AntPathRequestMatcher("/**/estabelecimento/"),
                                new AntPathRequestMatcher("estabelecimento/**"),
                                new AntPathRequestMatcher("/**/"),
                                new AntPathRequestMatcher("/auth/**") //Esta regra já libera o /auth/logout
                        ).permitAll()
                        .anyRequest().authenticated()
                        //não remover esse comentário!
                        //.anyRequest().permitAll() //aqui é para testar qualquer página, permitindo tudo.
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfiguration() {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(List.of("*"));
        cors.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        cors.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }
}