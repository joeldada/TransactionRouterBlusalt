package com.blusalt.crypto.service;

import com.blusalt.crypto.model.RsaKeypair;
import com.blusalt.crypto.repository.RsaKeypairRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class KeyServiceTest {

    private RsaKeypairRepo repo;
    private AesVault vault;
    private KeyService service;

    @BeforeEach
    void setup() {
        repo = mock(RsaKeypairRepo.class);
        vault = mock(AesVault.class);
        service = new KeyService(repo, vault);
    }

    @Test
    void ensureActiveKey_returnsExistingKey() {
        RsaKeypair existing = RsaKeypair.builder()
                .keyId("kid-123")
                .publicPem("PUBLIC")
                .privateCiphertext("cipher")
                .privateIv("iv")
                .alg("alg")
                .active(true)
                .build();

        when(repo.findByActiveTrue()).thenReturn(Optional.of(existing));

        RsaKeypair result = service.ensureActiveKey();

        assertEquals("kid-123", result.getKeyId());
        verify(repo, never()).save(any());
    }

    @Test
    void ensureActiveKey_generatesNewKeyWhenNoneActive() {
        when(repo.findByActiveTrue()).thenReturn(Optional.empty());
        when(vault.protect(any())).thenReturn(new String[]{"ciphertext", "iv"});

        ArgumentCaptor<RsaKeypair> captor = ArgumentCaptor.forClass(RsaKeypair.class);

        when(repo.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        RsaKeypair result = service.ensureActiveKey();

        assertNotNull(result.getKeyId());
        assertTrue(result.getPublicPem().startsWith("-----BEGIN PUBLIC KEY-----"));
        assertEquals("ciphertext", result.getPrivateCiphertext());
        assertEquals("iv", result.getPrivateIv());

        // Verify repo.save was called
        verify(repo).save(any(RsaKeypair.class));
    }

    @Test
    void publicKeyPem_returnsPem() {
        RsaKeypair existing = RsaKeypair.builder()
                .keyId("kid-xyz")
                .publicPem("-----BEGIN PUBLIC KEY-----\nabc\n-----END PUBLIC KEY-----")
                .privateCiphertext("cipher")
                .privateIv("iv")
                .alg("alg")
                .active(true)
                .build();

        when(repo.findByActiveTrue()).thenReturn(Optional.of(existing));

        String pem = service.publicKeyPem();

        assertTrue(pem.contains("PUBLIC KEY"));
    }

    @Test
    void loadPrivate_reconstructsPrivateKey() throws Exception {
        // Generate a fake RSA private key PEM
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();

        byte[] encoded = kp.getPrivate().getEncoded();
        String base64 = Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(encoded);
        String pem = "-----BEGIN PRIVATE KEY-----\n" + base64 + "\n-----END PRIVATE KEY-----";

        when(repo.findByActiveTrue()).thenReturn(Optional.of(RsaKeypair.builder()
                .keyId(UUID.randomUUID().toString())
                .publicPem("PUBLIC")
                .privateCiphertext("cipher")
                .privateIv("iv")
                .alg("alg")
                .active(true)
                .build()));

        when(vault.unprotect("cipher", "iv")).thenReturn(pem.getBytes());

        PrivateKey priv = service.loadPrivate();

        assertNotNull(priv);
        assertEquals("RSA", priv.getAlgorithm());
    }
}
