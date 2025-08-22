package com.blusalt.router.endpoint;


import com.blusalt.common.dto.RouteResponse;
import com.blusalt.common.dto.TransactionRequest;
import com.blusalt.common.utils.PanUtils;
import com.blusalt.router.dto.RouteRuleRequest;
import com.blusalt.router.model.RouteRule;
import com.blusalt.router.model.TxnLog;
import com.blusalt.router.repository.TxnLogRepo;
import com.blusalt.router.service.RouteRuleService;
import com.blusalt.router.service.RoutingEngine;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Validated
@RequiredArgsConstructor
public class RouterController {

    private final RoutingEngine engine;
    private final TxnLogRepo logRepo;
    private final ObjectMapper om = new ObjectMapper();
    @Value("${crypto.baseUrl}")
    String cryptoBase;

    private final RouteRuleService service;

    private RestClient rest() {
        return RestClient.builder().baseUrl(cryptoBase).build();
    }

    @PostMapping("/route/clear")
    public ResponseEntity<RouteResponse> routeClear(@Valid @RequestBody TransactionRequest req) throws Exception {
        if (req.getPan() == null || req.getPan().isBlank()) return ResponseEntity.badRequest().build();
        return process(req, req.getPan());
    }

    @PostMapping("/route/encrypted")
    public ResponseEntity<RouteResponse> routeEncrypted(@Valid @RequestBody TransactionRequest req) throws Exception {
        if (req.getEncryptedPan() == null || req.getEncryptedPan().isBlank())
            return ResponseEntity.badRequest().build();
        String pan = rest().post().uri("/crypto/decrypt").body(req.getEncryptedPan()).retrieve().body(String.class);
        return process(req, pan);
    }

    @PostMapping("/route")
    public ResponseEntity<RouteRule> addRoute(@Valid @RequestBody RouteRuleRequest request) {
        RouteRule saved = service.addRoute(request);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/route")
    public ResponseEntity<List<RouteRule>> getAllRoutes() {
        return ResponseEntity.ok(service.getAllRoutes());
    }

    @GetMapping("/route/{id}")
    public ResponseEntity<RouteRule> getRouteById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getRouteById(id));
    }

    private ResponseEntity<RouteResponse> process(TransactionRequest req, String pan) throws Exception {
        // validate PAN length again on decrypted
        if (pan == null || pan.length() < 12 || pan.length() > 19) return ResponseEntity.badRequest().build();
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("amount", req.getAmount());
        ctx.put("transactionType", req.getTransactionType());
        ctx.put("mcc", req.getMcc());
        ctx.put("country", req.getCountry());
        ctx.put("currency", req.getCurrency());
        ctx.put("cardPresent", req.getCardPresent());
        ctx.put("terminalType", req.getTerminalType());
        ctx.put("issuer", req.getIssuer());
        ctx.put("acquirer", req.getAcquirer());
        ctx.put("fraudScore", req.getFraudScore());

        RouteResponse resp = engine.route(pan, ctx).orElse(RouteResponse.builder().name("default").ip("127.0.0.1").port(9000).zpk("0000000000000000").build());
        persistLog(req, resp, true, PanUtils.bin6(pan) + ":" + PanUtils.detectScheme(pan));
        return ResponseEntity.ok(resp);
    }

    private void persistLog(TransactionRequest req, RouteResponse resp, boolean success, String rule) {
        try {
            var log = TxnLog.builder().createdAt(Instant.now()).success(success).decisionRule(rule)
                    .requestJson(om.writeValueAsString(req)).responseJson(om.writeValueAsString(resp)).build();
            logRepo.save(log);
        } catch (Exception ignored) {
        }
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "ok");
    }

}
