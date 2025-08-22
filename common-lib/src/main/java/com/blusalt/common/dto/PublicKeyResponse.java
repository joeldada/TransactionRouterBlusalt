package com.blusalt.common.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicKeyResponse {

    private String keyId;
    private String algorithm;
    private String publicKeyPem;

}
