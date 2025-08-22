package com.blusalt.router.service;

import com.blusalt.common.dto.RouteResponse;
import com.blusalt.router.model.RouteRule;
import com.blusalt.router.repository.RouteRuleRepo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RoutingEngineTest {

    @Test
    void picksHighestPriorityThenSpecificity() {
        RouteRule r1 = RouteRule.builder().name("A").ip("1.1.1.1").port(1).zpk("z").cardScheme("MASTERCARD").priority(5).enabled(true).build();
        RouteRule r2 = RouteRule.builder().name("B").ip("2.2.2.2").port(2).zpk("z").cardScheme("MASTERCARD").binPrefixCsv("539983").priority(5).enabled(true).build();
        RouteRule r3 = RouteRule.builder().name("C").ip("3.3.3.3").port(3).zpk("z").cardScheme("VISA").priority(1).enabled(true).build();
        RouteRuleRepo repo = Mockito.mock(RouteRuleRepo.class);
        Mockito.when(repo.findByEnabledTrueOrderByPriorityAsc()).thenReturn(List.of(r2, r1, r3));
        RoutingEngine eng = new RoutingEngine(repo);
        Map<String, Object> ctx = Map.of("amount", 150.0, "transactionType", "Purchase", "mcc", "5411", "country", "NG", "currency", "NGN", "cardPresent", true, "terminalType", "POS", "issuer", "Bank A", "acquirer", "Bank B", "fraudScore", 20);
        Optional<RouteResponse> rr = eng.route("5399831234567890", ctx);
        Assertions.assertTrue(rr.isPresent());
        Assertions.assertEquals("B", rr.get().getName());
    }

}
