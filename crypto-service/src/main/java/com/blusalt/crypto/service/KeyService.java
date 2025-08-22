package com.blusalt.crypto.service;

import com.blusalt.crypto.model.RsaKeypair;
import com.blusalt.crypto.repository.RsaKeypairRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KeyService {

    private final RsaKeypairRepo repo;
    private final AesVault vault;

    public RsaKeypair ensureActiveKey() {
        return repo.findByActiveTrue().orElseGet(this::generateNewKey);
    }

    private RsaKeypair generateNewKey() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair kp = kpg.generateKeyPair();
            String pubPem = toPem("PUBLIC KEY", kp.getPublic().getEncoded());
            String privPem = toPem("PRIVATE KEY", kp.getPrivate().getEncoded());
            String[] enc = vault.protect(privPem.getBytes());
            RsaKeypair entity = RsaKeypair.builder().keyId(UUID.randomUUID().toString()).publicPem(pubPem)
                    .privateCiphertext(enc[0]).privateIv(enc[1]).alg("RSA/ECB/OAEPWithSHA-256AndMGF1Padding").active(true).build();
            return repo.save(entity);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String publicKeyPem() {
        return ensureActiveKey().getPublicPem();
    }

    public String keyId() {
        return ensureActiveKey().getKeyId();
    }

    public PrivateKey loadPrivate() {
        try {
            RsaKeypair k = ensureActiveKey();
            byte[] privBytes = vault.unprotect(k.getPrivateCiphertext(), k.getPrivateIv());
            String pem = new String(privBytes);
            String base64 = pem.replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "").replaceAll("\\s+", "");
            byte[] der = Base64.getDecoder().decode(base64);
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(der));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String toPem(String type, byte[] encoded) {
        String base64 = Base64.getMimeEncoder(64, new byte[]{'\n'})
                .encodeToString(encoded);

        return "-----BEGIN " + type + "-----\n"
                + base64 + "\n"
                + "-----END " + type + "-----";
    }

}
