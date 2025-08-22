package com.blusalt.router.service;


import com.blusalt.common.dto.RouteResponse;
import com.blusalt.router.model.RouteRule;
import com.blusalt.router.repository.RouteRuleRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoutingEngine {

    private final RouteRuleRepo repo;
    public Optional<RouteResponse> route(String pan, Map<String,Object> ctx){
        var matcher = new RuleMatcher(pan, ctx);
        return repo.findByEnabledTrueOrderByPriorityAsc().stream()
                .filter(matcher::matches)
                .sorted(Comparator.comparingInt((RouteRule r)->r.getPriority()).thenComparing((RouteRule r)-> -matcher.specificity(r)))
                .findFirst()
                .map(r -> RouteResponse.builder().name(r.getName()).ip(r.getIp()).port(r.getPort()).zpk(r.getZpk()).build());
    }

}
