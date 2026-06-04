package com.company.component.auth.security;

import com.company.component.auth.core.JwtClaims;
import com.company.component.auth.core.JwtService;
import com.company.component.auth.properties.AuthProperties;
import com.company.component.auth.support.AuthPathMatcher;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * 从请求头解析 JWT 并写入 {@link SecurityContextHolder}。
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AuthProperties properties;
    private final AuthPathMatcher pathMatcher;

    public JwtAuthenticationFilter(JwtService jwtService, AuthProperties properties, AuthPathMatcher pathMatcher) {
        this.jwtService = jwtService;
        this.properties = properties;
        this.pathMatcher = pathMatcher;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return pathMatcher.matches(request);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = resolveToken(request);
        if (StringUtils.hasText(token)) {
            try {
                JwtClaims claims = jwtService.parseToken(token);
                var authentication = new UsernamePasswordAuthenticationToken(
                        claims.username(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER")));
                if (claims.userId() != null) {
                    authentication.setDetails(String.valueOf(claims.userId()));
                }
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtException ex) {
                SecurityContextHolder.clearContext();
                request.setAttribute("component.auth.jwt.error", ex.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(properties.getHeaderName());
        if (!StringUtils.hasText(header)) {
            return null;
        }
        String prefix = properties.getTokenPrefix();
        if (StringUtils.hasText(prefix) && header.startsWith(prefix)) {
            return header.substring(prefix.length()).trim();
        }
        return header.trim();
    }
}
