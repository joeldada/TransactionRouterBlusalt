package com.blusalt.router.service;

import com.blusalt.common.utils.PanUtils;
import com.blusalt.router.model.RouteRule;

import java.util.Arrays;
import java.util.Map;

public class RuleMatcher {

    private final String pan;
    private final String scheme;
    private final String bin6;
    private final Map<String, Object> ctx;

    public RuleMatcher(String pan, Map<String, Object> ctx) {
        this.pan = pan;
        this.scheme = PanUtils.detectScheme(pan);
        this.bin6 = PanUtils.bin6(pan);
        this.ctx = ctx;
    }

    public boolean matches(RouteRule r) {
        if (Boolean.FALSE.equals(r.isEnabled())) return false;
        if (r.getCardScheme() != null && !r.getCardScheme().equalsIgnoreCase(scheme)) return false;
        if (!csvBinMatch(r.getBinPrefixCsv(), bin6)) return false;
        Double amount = (Double) ctx.get("amount");
        if (r.getMinAmount() != null && amount < r.getMinAmount()) return false;
        if (r.getMaxAmount() != null && amount > r.getMaxAmount()) return false;
        if (!csvContains(r.getTransactionTypesCsv(), (String) ctx.get("transactionType"))) return false;
        if (!csvMccMatch(r.getMccCsv(), (String) ctx.get("mcc"))) return false;
        if (!csvContains(r.getCountriesCsv(), (String) ctx.get("country"))) return false;
        if (!csvContains(r.getCurrenciesCsv(), (String) ctx.get("currency"))) return false;
        if (r.getCardPresent() != null && !r.getCardPresent().equals(ctx.get("cardPresent"))) return false;
        if (!csvContains(r.getTerminalTypesCsv(), (String) ctx.get("terminalType"))) return false;
        if (!csvContains(r.getIssuersCsv(), (String) ctx.get("issuer"))) return false;
        if (!csvContains(r.getAcquirersCsv(), (String) ctx.get("acquirer"))) return false;
        Integer fs = (Integer) ctx.get("fraudScore");
        if (r.getMaxFraudScore() != null && fs != null && fs > r.getMaxFraudScore()) return false;
        return true;
    }

    public int specificity(RouteRule r) {
        // Higher specificity = more conditions set
        int s = 0;
        if (r.getCardScheme() != null) s++;
        if (r.getBinPrefixCsv() != null) s++;
        if (r.getMinAmount() != null || r.getMaxAmount() != null) s++;
        if (r.getTransactionTypesCsv() != null) s++;
        if (r.getMccCsv() != null) s++;
        if (r.getCountriesCsv() != null) s++;
        if (r.getCurrenciesCsv() != null) s++;
        if (r.getCardPresent() != null) s++;
        if (r.getTerminalTypesCsv() != null) s++;
        if (r.getIssuersCsv() != null) s++;
        if (r.getAcquirersCsv() != null) s++;
        if (r.getMaxFraudScore() != null) s++;
        return s;
    }

    private boolean csvContains(String csv, String v) {
        if (csv == null || csv.isBlank()) return true;
        if (v == null) return false;
        return Arrays.stream(csv.split(",")).map(String::trim).anyMatch(x -> x.equalsIgnoreCase(v));
    }

    private boolean csvMccMatch(String csv, String mcc) {
        if (csv == null || csv.isBlank()) return true;
        if (mcc == null) return false;
        return Arrays.stream(csv.split(",")).map(String::trim).anyMatch(x -> mcc.startsWith(x));
    }

    private boolean csvBinMatch(String csv, String bin) {
        if (csv == null || csv.isBlank()) return true;
        if (bin == null) return false;
        return Arrays.stream(csv.split(",")).map(String::trim).anyMatch(x -> {
            if (x.contains("-")) {
                String[] ab = x.split("-");
                return inRange(bin, ab[0], ab[1]);
            } else {
                return bin.startsWith(x);
            }
        });
    }

    private boolean inRange(String bin, String a, String b) {
        try {
            int i = Integer.parseInt(bin);
            return i >= Integer.parseInt(a) && i <= Integer.parseInt(b);
        } catch (Exception e) {
            return false;
        }
    }
}
