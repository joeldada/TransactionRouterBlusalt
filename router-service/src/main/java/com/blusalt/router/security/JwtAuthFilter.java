package com.blusalt.router.security;


import io.jsonwebtoken.Jwts;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class JwtAuthFilter implements Filter {

    @Value("${security.jwt.issuer}")
    String issuer;
    @Value("${security.jwt.publicKeyPem}")
    String publicPem;

    private PublicKey key() {
        try {
            String p = publicPem.replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "").replaceAll("\\s+", "");
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(p)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest r = (HttpServletRequest) req;
        HttpServletResponse w = (HttpServletResponse) res;
        if (r.getRequestURI().startsWith("/actuator") || (r.getRequestURI().equals("/api/health"))) {
            chain.doFilter(req, res);
            return;
        }
        String auth = r.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            w.sendError(401, "missing_token");
            return;
        }
        String token = auth.substring(7);
        try {
            Jwts.parserBuilder().requireIssuer(issuer).setSigningKey(key()).build().parseClaimsJws(token);
            chain.doFilter(req, res);
        } catch (Exception e) {
            w.sendError(401, "invalid_token");
        }
    }

}
