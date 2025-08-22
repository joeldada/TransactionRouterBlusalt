package com.blusalt.crypto.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "rsa_keypairs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RsaKeypair {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String keyId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String publicPem; // clear is okay (public)

    @Column(columnDefinition = "TEXT", nullable = false)
    private String privateCiphertext; // AES-GCM(base64)

    @Column(nullable = false)
    private String privateIv; // base64

    @Column(nullable = false)
    private String alg;

    private boolean active;

}
