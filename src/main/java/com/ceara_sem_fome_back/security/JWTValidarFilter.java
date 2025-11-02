package com.ceara_sem_fome_back.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.ceara_sem_fome_back.service.PessoaDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class JWTValidarFilter extends BasicAuthenticationFilter {

    public static final String HEADER_ATRIBUTO = "Authorization";
    public static final String ATRIBUTO_PREFIXO = "Bearer ";

    private final String tokenSenha;

    // --- CORREÇÃO: agora usa PessoaDetailsService ---
    private final PessoaDetailsService pessoaDetailsService;

    private static final List<String> ROTAS_PUBLICAS = Arrays.asList(
            "/beneficiario/iniciar-cadastro",
            "/beneficiario/login",
            "/adm/login",
            "/token/confirmar-cadastro",
            "/auth/**"
    );

    public JWTValidarFilter(AuthenticationManager authenticationManager,
                            String tokenSenha,
                            PessoaDetailsService pessoaDetailsService) {
        super(authenticationManager);
        this.tokenSenha = tokenSenha;
        this.pessoaDetailsService = pessoaDetailsService;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
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
        String ipAddress = request.getRemoteAddr();

        try {
            String email = getSubjectFromToken(token);
            UserDetails userDetails = pessoaDetailsService.loadUserByUsername(email);

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            chain.doFilter(request, response);

        } catch (JWTVerificationException e) {
            log.warn("FALHA TOKEN: {}. IP: {}. Token: {}", e.getMessage(), ipAddress, token);
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(
                    "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Token inválido ou expirado. Faça login novamente.\"}"
            );
        }
    }

    private String getSubjectFromToken(String token) {
        String usuario = JWT.require(Algorithm.HMAC512(tokenSenha))
                .build()
                .verify(token)
                .getSubject();

        if (usuario == null) {
            throw new JWTVerificationException("Token válido, mas o 'subject' (usuário) está nulo.");
        }
        return usuario;
    }
}
