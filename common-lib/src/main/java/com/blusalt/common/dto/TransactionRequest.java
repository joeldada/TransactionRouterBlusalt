package com.blusalt.common.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionRequest {

    // Either pan or encryptedPan must be provided, validated in controller
    @Size(min = 12, max = 19)
    private String pan;

    private String encryptedPan; // Base64 RSA/OAEP payload

    @NotNull
    @Positive
    private Double amount;

    @NotBlank
    private String transactionType; // Purchase, Refund, etc.

    @Pattern(regexp = "^\\d{4}$", message = "MCC must be 4 digits")
    private String mcc;

    @Pattern(regexp = "^[A-Z]{2}$")
    private String country; // ISO2

    @Pattern(regexp = "^[A-Z]{3}$")
    private String currency; // ISO3

    @NotNull
    private Boolean cardPresent;

    @NotBlank
    private String terminalType; // ATM, POS, ECOM

    @NotBlank
    private String issuer;

    @NotBlank
    private String acquirer;

    @Min(0) @Max(100)
    private Integer fraudScore;

}
