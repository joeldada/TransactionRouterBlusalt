package com.blusalt.common.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class PanUtilsTest {

    @Test
    void testDetectSchemeVisa() {
        assertEquals("VISA", PanUtils.detectScheme("4123456789012345"));
        assertEquals("VISA", PanUtils.detectScheme("4"));
    }

    @Test
    void testDetectSchemeMastercard() {
        assertEquals("MASTERCARD", PanUtils.detectScheme("5123456789012345"));
        assertEquals("MASTERCARD", PanUtils.detectScheme("5512345678901234"));
    }

    @Test
    void testDetectSchemeVerve() {
        assertEquals("VERVE", PanUtils.detectScheme("5061234567890123"));
    }

    @Test
    void testDetectSchemeUnknown() {
        assertEquals("UNKNOWN", PanUtils.detectScheme("6011123456789012")); // Discover-like
        assertEquals("UNKNOWN", PanUtils.detectScheme(""));                 // Empty string
        assertEquals("UNKNOWN", PanUtils.detectScheme(null));               // Null
    }

    @Test
    void testBin6Valid() {
        assertEquals("412345", PanUtils.bin6("4123456789012345"));
        assertEquals("506123", PanUtils.bin6("5061234567890123"));
    }

    @Test
    void testBin6TooShort() {
        assertNull(PanUtils.bin6("12345"));  // less than 6 digits
        assertNull(PanUtils.bin6(null));     // null input
    }
}
