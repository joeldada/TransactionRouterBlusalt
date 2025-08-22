package com.blusalt.auth.dto;


import lombok.Data;

@Data
public class CreateClientRequest {

    private String clientId;
    private String secret;

}
