package com.blusalt.crypto.service;


import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class AesVault {

    @Value("${crypto.aesKey}")
    private String aesKeyB64;
    private SecretKey key;
    private final SecureRandom rnd = new SecureRandom();

    @PostConstruct
    void init() {
        byte[] b = Base64.getDecoder().decode(aesKeyB64);
        key = new SecretKeySpec(b, "AES");
    }

    public String[] protect(byte[] plaintext) {
        try {
            byte[] iv = new byte[12];
            rnd.nextBytes(iv);
            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            c.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
            byte[] ct = c.doFinal(plaintext);
            return new String[]{Base64.getEncoder().encodeToString(ct), Base64.getEncoder().encodeToString(iv)};
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] unprotect(String ctB64, String ivB64) {
        try {
            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            c.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, Base64.getDecoder().decode(ivB64)));
            return c.doFinal(Base64.getDecoder().decode(ctB64));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
