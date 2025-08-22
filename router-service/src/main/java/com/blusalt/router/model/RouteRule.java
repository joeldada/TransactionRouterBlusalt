package com.blusalt.router.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "route_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String ip;

    @Column(nullable = false)
    private Integer port;

    @Column(nullable = false)
    private String zpk;

    private String cardScheme; // VISA/MASTERCARD/VERVE
    private String binPrefixCsv; // e.g., 539983,512345-512399; supports range & list
    private Double minAmount;
    private Double maxAmount;
    private String transactionTypesCsv; // Purchase,Refund
    private String mccCsv; // 5411,5999,30xx (supports prefix)
    private String countriesCsv; // NG,GH
    private String currenciesCsv; // NGN,USD
    private Boolean cardPresent;
    private String terminalTypesCsv; // POS,ATM,ECOM
    private String issuersCsv;
    private String acquirersCsv;
    private Integer maxFraudScore; // route only if score <= max

    @Column(nullable = false)
    private Integer priority; // lower number = higher priority
    @Column(nullable = false)
    private boolean enabled;

}
