package com.blusalt.router.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "txn_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TxnLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String requestJson;

    @Column(columnDefinition = "TEXT")
    private String responseJson;

    @Column(nullable = false)
    private Instant createdAt;

    private String decisionRule;

    private boolean success;

}
