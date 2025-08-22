package com.blusalt.auth.endpoint;

import com.blusalt.auth.dto.ApiClientResponse;
import com.blusalt.auth.dto.CreateClientRequest;
import com.blusalt.auth.model.ApiClient;
import com.blusalt.auth.repository.ApiClientRepo;
import com.blusalt.common.dto.JwtTokenResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthControllerTest {

    @Mock
    private ApiClientRepo repo;

    @InjectMocks
    private AuthController controller;

    private String privatePem;
    private ApiClient mockClient;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Generate RSA key for testing
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair keyPair = kpg.generateKeyPair();
        byte[] pkcs8 = keyPair.getPrivate().getEncoded();
        privatePem = "-----BEGIN PRIVATE KEY-----\n" +
                Base64.getEncoder().encodeToString(pkcs8) +
                "\n-----END PRIVATE KEY-----";

        // Inject values
        controller = new AuthController(repo);
        controller.setIssuer("txn-routing-auth");
        controller.setTtl(3600);
        controller.setPrivatePem(privatePem);

        // Mock client
        String hashed = BCrypt.hashpw("secret123", BCrypt.gensalt());
        mockClient = ApiClient.builder()
                .id(1L)
                .clientId("client-1")
                .secretHash(hashed)
                .active(true)
                .build();
    }

    @Test
    void testTokenSuccess() {
        when(repo.findByClientIdAndActiveTrue("client-1"))
                .thenReturn(Optional.of(mockClient));

        ResponseEntity<JwtTokenResponse> response = controller.token("client-1", "secret123");

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getAccessToken());
        assertEquals(3600, response.getBody().getExpiresIn());
        verify(repo, times(1)).findByClientIdAndActiveTrue("client-1");
    }

    @Test
    void testTokenInvalidClient() {
        when(repo.findByClientIdAndActiveTrue("bad-client"))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> controller.token("bad-client", "secret123"));

        assertEquals("invalid_client", ex.getMessage());
    }

    @Test
    void testTokenInvalidSecret() {
        when(repo.findByClientIdAndActiveTrue("client-1"))
                .thenReturn(Optional.of(mockClient));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> controller.token("client-1", "wrong-secret"));

        assertEquals("invalid_secret", ex.getMessage());
    }

    @Test
    void testCreateClientSuccess() {
        ApiClient saved = ApiClient.builder()
                .id(2L)
                .clientId("new-client")
                .secretHash("hashed-secret")
                .active(true)
                .build();

        when(repo.save(any(ApiClient.class))).thenReturn(saved);

        CreateClientRequest createClientRequest = new CreateClientRequest();
        createClientRequest.setClientId("new-client");
        createClientRequest.setSecret("new-secret");
        ResponseEntity<ApiClientResponse> response = controller.createClient(createClientRequest);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("new-client", response.getBody().getClientId());
        verify(repo, times(1)).save(any(ApiClient.class));
    }

}
