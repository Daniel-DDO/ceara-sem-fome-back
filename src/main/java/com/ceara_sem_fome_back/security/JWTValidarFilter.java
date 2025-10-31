package com.ceara_sem_fome_back.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException; 
import com.ceara_sem_fome_back.service.ComercianteService; // <<< IMPORTADO
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails; // <<< IMPORTADO
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
    
    // --- CORREÇÃO: Adicionado o Service ---
    private final ComercianteService comercianteService;

    //Lista de rotas públicas que o filtro deve IGNORAR
    // (A rota /produtos/cadastrar foi REMOVIDA daqui, como fizemos antes)
    private static final List<String> ROTAS_PUBLICAS = Arrays.asList(
            "/beneficiario/iniciar-cadastro",
            "/beneficiario/login",
            "/adm/login",
            "/token/confirmar-cadastro",
            "/auth/**" //Ignora TODAS as rotas de /auth (recuperação de senha)
    );

    // --- CORREÇÃO: Construtor atualizado ---
    public JWTValidarFilter(AuthenticationManager authenticationManager, 
                            String tokenSenha, 
                            ComercianteService comercianteService) { // <<< Adicionado
        super(authenticationManager);
        this.tokenSenha = tokenSenha;
        this.comercianteService = comercianteService; // <<< Adicionado
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
        String ipAddress = request.getRemoteAddr(); 

        try {
            // --- CORREÇÃO: Carregar o UserDetails completo ---
            
            // 1. Pega o e-mail (Subject) do token
            String email = getSubjectFromToken(token);
            
            // 2. Usa o e-mail para carregar o objeto ComercianteData
            UserDetails userDetails = comercianteService.loadUserByUsername(email);
            
            // 3. Cria o token de autenticação com o objeto ComercianteData (e não só com a String)
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            
            // --- FIM DA CORREÇÃO ---

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            
            // Se a autenticação foi bem-sucedida, continue para o próximo filtro
            chain.doFilter(request, response); 

        } catch (JWTVerificationException e) { 
            
            log.warn(
                    "FALHA TOKEN: Tentativa de validação de token falhou. Motivo: {}. IP: {}. Token: {}",
                    e.getMessage(), 
                    ipAddress,
                    token
            );
            SecurityContextHolder.clearContext();

            // (A correção do 'catch' que fizemos antes está mantida)
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(
                "{\"status\": 401, \"error\": \"Unauthorized\", \"message\": \"Token inválido ou expirado. Faça login novamente.\"}"
            );
            return; // NÃO chame chain.doFilter()
        }
    }

    // Método renomeado (antes era getAuthenticationToken)
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