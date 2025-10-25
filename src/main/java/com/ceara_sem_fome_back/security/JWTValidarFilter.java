package com.ceara_sem_fome_back.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j; 
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j 
public class JWTValidarFilter extends BasicAuthenticationFilter {

    public static final String HEADER_ATRIBUTO = "Authorization";
    public static final String ATRIBUTO_PREFIXO = "Bearer ";

    private final String tokenSenha;
    
    //Lista de rotas públicas que o filtro deve IGNORAR
    private static final List<String> ROTAS_PUBLICAS = Arrays.asList(
        "/beneficiario/iniciar-cadastro",
        "/beneficiario/login",
        "/adm/login",
        "/token/confirmar-cadastro",
        "/auth/**" //Ignora TODAS as rotas de /auth (recuperação de senha)
    );

    public JWTValidarFilter(AuthenticationManager authenticationManager, String tokenSenha) {
        super(authenticationManager);
        this.tokenSenha = tokenSenha;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) throws ServletException {
        AntPathMatcher pathMatcher = new AntPathMatcher();
        String requestURI = request.getRequestURI();
        return ROTAS_PUBLICAS.stream().anyMatch(rota -> pathMatcher.match(rota, requestURI));
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws IOException, ServletException {
        
        String atributo = request.getHeader(HEADER_ATRIBUTO);

        if (atributo == null || !atributo.startsWith(ATRIBUTO_PREFIXO)) {
            chain.doFilter(request, response);
            return;
        }

        String token = atributo.replace(ATRIBUTO_PREFIXO, "");
        String ipAddress = request.getRemoteAddr(); //Captura IP
        
        try {
            UsernamePasswordAuthenticationToken authenticationToken = getAuthenticationToken(token);
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        } catch (Exception e) {
            
            //LOG DE FALHA DE VALIDAÇÃO DE TOKEN
            log.warn(
                "FALHA TOKEN: Tentativa de validação de token falhou. Motivo: {}. IP: {}. Token: {}",
                e.getMessage(), //Ex.: "O token expirou"
                ipAddress,
                token
            );
            SecurityContextHolder.clearContext();
        }
        chain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken getAuthenticationToken(String token) {
        String usuario = JWT.require(Algorithm.HMAC512(tokenSenha))
                .build()
                .verify(token)
                .getSubject();

        if (usuario == null) {
            return null;
        }

        return new UsernamePasswordAuthenticationToken(usuario, null, new ArrayList<>());
    }
}