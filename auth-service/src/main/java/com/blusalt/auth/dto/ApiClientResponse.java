package com.blusalt.auth.dto;


import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class ApiClientResponse {

    private final String clientId;
    private final String secret;

}
