package com.blusalt.router.service;

import com.blusalt.router.dto.RouteRuleRequest;
import com.blusalt.router.model.RouteRule;
import com.blusalt.router.repository.RouteRuleRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RouteRuleService {

    private final RouteRuleRepo repo;

    public RouteRule addRoute(RouteRuleRequest req) {
        RouteRule rule = RouteRule.builder()
                .name(req.getName())
                .ip(req.getIp())
                .port(req.getPort())
                .zpk(req.getZpk())
                .cardScheme(req.getCardScheme())
                .binPrefixCsv(req.getBinPrefixCsv())
                .minAmount(req.getMinAmount())
                .maxAmount(req.getMaxAmount())
                .transactionTypesCsv(req.getTransactionTypesCsv())
                .mccCsv(req.getMccCsv())
                .countriesCsv(req.getCountriesCsv())
                .currenciesCsv(req.getCurrenciesCsv())
                .cardPresent(req.getCardPresent())
                .terminalTypesCsv(req.getTerminalTypesCsv())
                .issuersCsv(req.getIssuersCsv())
                .acquirersCsv(req.getAcquirersCsv())
                .maxFraudScore(req.getMaxFraudScore())
                .priority(req.getPriority())
                .enabled(req.isEnabled())
                .build();

        return repo.save(rule);
    }

    public List<RouteRule> getAllRoutes() {
        return repo.findAll();
    }

    public RouteRule getRouteById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Route not found with id " + id));
    }

}
