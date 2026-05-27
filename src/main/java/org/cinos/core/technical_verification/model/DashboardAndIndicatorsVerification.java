package org.cinos.core.technical_verification.model;
import jakarta.persistence.Embeddable;
import lombok.*;
import org.cinos.core.technical_verification.utils.ConditionEvaluator;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardAndIndicatorsVerification {

    private Double checkEngineLight;
    private Double absLight;
    private Double batteryLight;
    private Double airbagLight;
    private Double speedometerAndTachometer;
    private Double dashboardLighting;

    public ConditionStatus getCheckEngineStatus() {
        return ConditionEvaluator.fromPercentage(checkEngineLight);
    }

    public ConditionStatus getAbsStatus() {
        return ConditionEvaluator.fromPercentage(absLight);
    }

    public ConditionStatus getBatteryStatus() {
        return ConditionEvaluator.fromPercentage(batteryLight);
    }

    public ConditionStatus getAirbagStatus() {
        return ConditionEvaluator.fromPercentage(airbagLight);
    }

    public ConditionStatus getInstrumentsStatus() {
        return ConditionEvaluator.fromPercentage(speedometerAndTachometer);
    }

    public ConditionStatus getLightingStatus() {
        return ConditionEvaluator.fromPercentage(dashboardLighting);
    }

    public double averageScore() {
        return (
                checkEngineLight +
                        absLight +
                        batteryLight +
                        airbagLight +
                        speedometerAndTachometer +
                        dashboardLighting
        ) / 6.0;
    }

    public ConditionStatus getOverallStatus() {
        return ConditionEvaluator.fromPercentage(averageScore());
    }
}
