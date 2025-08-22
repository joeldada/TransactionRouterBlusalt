package com.blusalt.crypto.endpoint;


import com.blusalt.common.dto.PublicKeyResponse;
import com.blusalt.crypto.service.KeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Cipher;
import java.util.Base64;

@RestController
@RequestMapping("/crypto")
@RequiredArgsConstructor
public class CryptoController {

    private final KeyService keys;

    @GetMapping("/keys/public")
    public ResponseEntity<PublicKeyResponse> getPublic() {
        return ResponseEntity.ok(new PublicKeyResponse(keys.keyId(), "RSA-OAEP-256", keys.publicKeyPem()));
    }

    @PostMapping(value = "/decrypt", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> decrypt(@RequestBody String encryptedB64) {
        try {
            var privateKey = keys.loadPrivate();
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] plain = cipher.doFinal(Base64.getDecoder().decode(encryptedB64));
            return ResponseEntity.ok(new String(plain));
        } catch (Exception e) {
            throw new RuntimeException("decrypt_failed");
        }
    }
}
