package com.ceara_sem_fome_back.security;

import com.ceara_sem_fome_back.service.PessoaDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class JWTConfiguracao {

    @Value("${api.guid.token.senha}")
    private String tokenSenha;

    @Autowired
    private PessoaDetailsService pessoaDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LogoutSuccessHandler loggingLogoutSuccessHandler;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            org.springframework.security.config.annotation.web.builders.HttpSecurity http,
            AuthenticationManager authManager
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable()) // Desativa CSRF para APIs REST
                .cors(cors -> cors.disable()) // Desabilita o CORS interno (vamos usar o filtro global)
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Sem sessões
                .addFilter(new JWTAutenticarFilter(authManager, tokenSenha)) // Filtro de autenticação (login)
                .addFilter(new JWTValidarFilter(authManager, tokenSenha, pessoaDetailsService)) // Filtro de validação JWT
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/auth/logout"))
                        .logoutSuccessHandler(loggingLogoutSuccessHandler)
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .requestMatchers(
                                "/beneficiario/iniciar-cadastro",
                                "/beneficiario/login",
                                "/adm/login",
                                "/token/confirmar-cadastro",
                                "/auth/**"
                        ).permitAll()

                        .anyRequest().authenticated()
                );

        // Desabilita proteção a frames (útil caso use H2 Console)
        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }
}

//
//package com.ceara_sem_fome_back.security;
//
//import com.ceara_sem_fome_back.security.Handler.LoggingLogoutSuccessHandler;
//import com.ceara_sem_fome_back.service.PessoaDetailsService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.CorsConfigurationSource;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//
//import java.util.List;
//
//@Configuration
//public class JWTConfiguracao {
//
//    @Value("${api.guid.token.senha}")
//    private String tokenSenha;
//
//    @Autowired
//    private LoggingLogoutSuccessHandler loggingLogoutSuccessHandler;
//
//    @Autowired
//    private PessoaDetailsService pessoaDetailsService;
//
//    @Autowired
//    private PasswordEncoder passwordEncoder;
//
//    @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
//        return authConfig.getAuthenticationManager();
//    }


//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http,
//                                                   AuthenticationManager authManager) throws Exception {
//
//        http
//                .cors(cors -> cors.configurationSource(corsConfiguration()))
//                .csrf(csrf -> csrf.disable())
//                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//
//                .addFilter(new JWTAutenticarFilter(authManager, tokenSenha))
//                .addFilter(new JWTValidarFilter(authManager, tokenSenha, pessoaDetailsService))
//
//                .logout(logout -> logout
//                        .logoutUrl("/auth/logout")
//                        .logoutSuccessHandler(loggingLogoutSuccessHandler)
//                        .invalidateHttpSession(true)
//                        .deleteCookies("JSESSIONID")
//                )
//
//                .authorizeHttpRequests(auth -> auth
//                                .requestMatchers(
//                                        new AntPathRequestMatcher("/*/iniciar-cadastro"),
//                                        new AntPathRequestMatcher("/*/login"),
//                                        new AntPathRequestMatcher("/token/confirmar-cadastro"),
//                                        new AntPathRequestMatcher("/**/all"),
//                                        new AntPathRequestMatcher("/version"),
//                                        new AntPathRequestMatcher("/health"),
//                                        new AntPathRequestMatcher("/**/meu-perfil"),
//                                        new AntPathRequestMatcher("/**/estabelecimento/"),
//                                        new AntPathRequestMatcher("estabelecimento/**"),
//                                        new AntPathRequestMatcher("/**/"),
//                                        new AntPathRequestMatcher("/auth/**"),
//                                        new AntPathRequestMatcher("/**/bairro/**"),
//                                        new AntPathRequestMatcher("/**/municipio/**"),
//                                        new AntPathRequestMatcher("/beneficiario/cadastrar-endereco"),
//                                        new AntPathRequestMatcher("/estabelecimento/cadastrar-endereco")
//                                ).permitAll()
//                                .anyRequest().authenticated()
//                        //não remover esse comentário!
//                        //.anyRequest().permitAll() //para testes, permitir tudo
//                );
//
//        return http.build();
//    }
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authManager) throws Exception {
//
//        http.cors().and()
//                .csrf(csrf -> csrf.disable())
//                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//
//                .addFilter(new JWTAutenticarFilter(authManager, tokenSenha))
//                .addFilter(new JWTValidarFilter(authManager, tokenSenha, pessoaDetailsService))
//
//                .logout(logout -> logout
//                        .logoutUrl("/auth/logout")
//                        .logoutSuccessHandler(loggingLogoutSuccessHandler)
//                        .invalidateHttpSession(true)
//                        .deleteCookies("JSESSIONID")
//                )
//
//                .authorizeHttpRequests(auth -> auth
//                                .requestMatchers("/**").permitAll()
//                );
//
//        return http.build();
//    }
//
//
//    @Bean
//    public CorsConfigurationSource corsConfiguration() {
//        CorsConfiguration cors = new CorsConfiguration();
//
//        cors.setAllowedOriginPatterns(List.of("*"));
//        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
//        cors.setAllowedHeaders(List.of("*"));
//        cors.setAllowCredentials(true);
//        cors.setExposedHeaders(List.of("Authorization", "Content-Type"));
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", cors);
//        return source;
//    }
//}
