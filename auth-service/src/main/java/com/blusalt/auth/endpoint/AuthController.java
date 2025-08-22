package com.blusalt.auth.endpoint;


import com.blusalt.auth.dto.ApiClientResponse;
import com.blusalt.auth.dto.CreateClientRequest;
import com.blusalt.auth.model.ApiClient;
import com.blusalt.auth.repository.ApiClientRepo;
import com.blusalt.common.dto.JwtTokenResponse;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@RestController
@RequestMapping("/oauth")
@RequiredArgsConstructor
public class AuthController {

    private final ApiClientRepo repo;

    @Value("${security.jwt.issuer}")
    private String issuer;

    @Value("${security.jwt.ttlSeconds}")
    private long ttl;

    @Value("${security.jwt.privateKeyPem}")
    private String privatePem;


    @PostMapping("/token")
    public ResponseEntity<JwtTokenResponse> token(
            @RequestHeader("X-Client-Id") String clientId,
            @RequestHeader("X-Client-Secret") String clientSecret) {

        // Validate client
        ApiClient client = repo.findByClientIdAndActiveTrue(clientId)
                .orElseThrow(() -> new RuntimeException("invalid_client"));

        if (!BCrypt.checkpw(clientSecret, client.getSecretHash())) {
            throw new RuntimeException("invalid_secret");
        }

        // Load signing key
        PrivateKey key = loadPrivateKey(privatePem);

        // Build JWT
        String jwt = generateJwtToken(clientId, key);

        return ResponseEntity.ok(new JwtTokenResponse(jwt, ttl));
    }

    @PostMapping("/clients")
    public ResponseEntity<ApiClientResponse> createClient(@RequestBody CreateClientRequest req) {
        // If clientId not provided, generate UUID
        String clientId = (req.getClientId() != null && !req.getClientId().isBlank())
                ? req.getClientId()
                : UUID.randomUUID().toString();

        // Hash secret
        String secretHash = BCrypt.hashpw(req.getSecret(), BCrypt.gensalt());

        ApiClient client = ApiClient.builder()
                .clientId(clientId)
                .secretHash(secretHash)
                .active(true)
                .build();

        repo.save(client);

        return ResponseEntity.ok(new ApiClientResponse(clientId, req.getSecret()));
    }

    private String generateJwtToken(String clientId, PrivateKey key) {
        Instant now = Instant.now();

        return Jwts.builder()
                .setIssuer(issuer)
                .setSubject(clientId)
                .claim("scope", "route:write")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(ttl)))
                .signWith(key, SignatureAlgorithm.RS256) // ✅ specify algorithm for RSA
                .compact();
    }

    private PrivateKey loadPrivateKey(String pem) {
        try {
            String normalizedPem = pem
                    .replaceAll("-----BEGIN (.*)-----", "")
                    .replaceAll("-----END (.*)-----", "")
                    .replaceAll("\\s+", "");
            byte[] keyBytes = Base64.getDecoder().decode(normalizedPem);
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load private key", e);
        }
    }

    // --- setters for testing only ---
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public void setTtl(long ttl) {
        this.ttl = ttl;
    }

    public void setPrivatePem(String privatePem) {
        this.privatePem = privatePem;
    }
}
