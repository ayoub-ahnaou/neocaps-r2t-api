package com.neocaps.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class DecayServiceTest {

    private DecayService decayService;

    @BeforeEach
    void setUp() {
        decayService = new DecayService();
        // Inject half-life of 192.48 hours (I-131)
        ReflectionTestUtils.setField(decayService, "halfLifeHours", 192.48);
    }

    @Test
    void testCalculateRequiredVolume_NoDecay() {
        LocalDate calDate = LocalDate.of(2026, 5, 29);
        
        // Target date is identical to calibration date (time difference = 0)
        double volume = decayService.calculateRequiredVolume(10.0, calDate, calDate, 50.0);
        
        // V = 50.0 / 10.0 = 5.0 µL
        assertEquals(5.0, volume, 0.0001);
    }

    @Test
    void testCalculateRequiredVolume_OneHalfLifeDecay() {
        LocalDate calDate = LocalDate.of(2026, 5, 29);
        
        // Target date is exactly 1 half-life (approx 8 days) later
        double volume = decayService.calculateRequiredVolume(10.0, calDate, calDate.plusDays(8), 50.0);
        
        // Since 1 half-life elapsed, concentration fell by ~50% (from 10.0 to 5.0 mCi/µL)
        // V = 50.0 / 5.0 = 10.0 µL
        assertEquals(10.0, volume, 0.1);
    }

    @Test
    void testCalculateRequiredVolume_FutureCalibration() {
        LocalDate calDate = LocalDate.of(2026, 5, 29);
        
        // Target date is 24 hours BEFORE lot calibration date
        // Since target is in the past, the liquid is stronger at target date
        double vol = decayService.calculateRequiredVolume(10.0, calDate, calDate.minusDays(1), 50.0);
        
        // Volume should be LESS than 5.0 µL because the liquid is more concentrated
        assertTrue(vol < 5.0);
        assertTrue(vol > 0.0);
    }

    @Test
    void testCalculateRequiredVolume_InvalidDate() {
        LocalDate calDate = LocalDate.of(2026, 5, 29);
        LocalDate targetDate = calDate.plusYears(100); // extreme decay (100 years)

        // Very long duration: concentration decays to effectively zero, raising IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            decayService.calculateRequiredVolume(1.0, calDate, targetDate, 1.0);
        });
    }
}
