package com.neocaps.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class DecayService {

    @Value("${app.decay.half-life-hours:192.48}")
    private double halfLifeHours; // Default half-life of Iodine-131 (approx 8.02 days)

    /**
     * Calculates the required liquid volume (in microliters) of a radioactive source
     * to deliver a specific dose (in mCi) at a target calibration date, taking into
     * account radioactive decay since the lot's calibration date.
     *
     * Formula:
     * C_capsule = C_lot * e^(-lambda * t)
     * V = requestedDose / C_capsule
     *
     * @param lotConcentration          Concentration of the lot liquid at lot calibration date (mCi/µL)
     * @param lotCalibrationDate        The date and time the lot concentration was calibrated
     * @param capsuleCalibrationDate    The target calibration date and time requested for the capsule
     * @param requestedDoseMci          The requested activity dose for the capsule (mCi)
     * @return Required volume in microliters (µL)
     */
    public double calculateRequiredVolume(double lotConcentration, 
                                           LocalDate lotCalibrationDate,
                                           LocalDate capsuleCalibrationDate,
                                           double requestedDoseMci) {
        // Calculate elapsed time in hours between lot calibration and target capsule calibration
        double timeDiffHours = (double) Duration.between(lotCalibrationDate.atStartOfDay(), capsuleCalibrationDate.atStartOfDay()).toMillis() / 3600000.0;
        
        // Decay constant (lambda) = ln(2) / half-life
        double lambda = Math.log(2.0) / halfLifeHours;
        
        // Concentration at capsule calibration date
        double concentrationAtCapsuleDate = lotConcentration * Math.exp(-lambda * timeDiffHours);
        
        if (concentrationAtCapsuleDate <= 0.0) {
            throw new IllegalArgumentException("Calculated concentration is zero or negative due to decay.");
        }
        
        // Required volume in microliters = dose / concentration
        return requestedDoseMci / concentrationAtCapsuleDate;
    }

    public double getHalfLifeHours() {
        return halfLifeHours;
    }
}
