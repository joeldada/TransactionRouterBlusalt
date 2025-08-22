package com.blusalt.router.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RouteRuleRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String ip;

    @NotNull
    @Min(1) @Max(65535)
    private Integer port;

    @NotBlank
    private String zpk;

    private String cardScheme;
    private String binPrefixCsv;

    @DecimalMin("0.0")
    private Double minAmount;

    @DecimalMin("0.0")
    private Double maxAmount;

    private String transactionTypesCsv;
    private String mccCsv;
    private String countriesCsv;
    private String currenciesCsv;

    private Boolean cardPresent;

    private String terminalTypesCsv;
    private String issuersCsv;
    private String acquirersCsv;

    @Min(0)
    private Integer maxFraudScore;

    @NotNull
    private Integer priority;

    private boolean enabled;
}
