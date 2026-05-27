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
public class BrakingSystemVerification {

    private Double brakePadsAndDiscs;
    private Double handbrakeFunctionality;
    private Double brakePedalPressure;
    private Double brakeResponse;
    private Double brakeFluidLevel;

    public ConditionStatus getPadsAndDiscsStatus() {
        return ConditionEvaluator.fromPercentage(brakePadsAndDiscs);
    }

    public ConditionStatus getHandbrakeStatus() {
        return ConditionEvaluator.fromPercentage(handbrakeFunctionality);
    }

    public ConditionStatus getPedalPressureStatus() {
        return ConditionEvaluator.fromPercentage(brakePedalPressure);
    }

    public ConditionStatus getBrakeResponseStatus() {
        return ConditionEvaluator.fromPercentage(brakeResponse);
    }

    public ConditionStatus getFluidLevelStatus() {
        return ConditionEvaluator.fromPercentage(brakeFluidLevel);
    }

    public double averageScore() {
        return (
                brakePadsAndDiscs +
                        handbrakeFunctionality +
                        brakePedalPressure +
                        brakeResponse +
                        brakeFluidLevel
        ) / 5.0;
    }

    public ConditionStatus getOverallStatus() {
        return ConditionEvaluator.fromPercentage(averageScore());
    }
}
