package org.cinos.core.technical_verification.model;

import jakarta.persistence.*;
import lombok.*;
import org.cinos.core.technical_verification.utils.ConditionEvaluator;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
@Builder
public class MotorVerification {

    // Estado general del motor (ruidos, vibraciones) (0 a 100%)
    private Double generalEngineCondition;
    // Pérdidas de aceite o líquidos (0 a 100%)
    private Double fluidLeaks;
    // Estado de correas y mangueras (0 a 100%)
    private Double beltsAndHosesCondition;
    // Arranque en frío y en caliente (0 a 100%)
    private Double engineStartPerformance;
    // Color y nivel del aceite (0 a 100%)
    private Double oilCondition;
    // Estado del radiador y líquido refrigerante (0 a 100%)
    private Double coolingSystemCondition;

    public ConditionStatus getGeneralEngineStatus() {
        return ConditionEvaluator.fromPercentage(generalEngineCondition);
    }

    public ConditionStatus getFluidLeaksStatus() {
        return ConditionEvaluator.fromPercentage(fluidLeaks);
    }

    public ConditionStatus getBeltsAndHosesStatus() {
        return ConditionEvaluator.fromPercentage(beltsAndHosesCondition);
    }

    public ConditionStatus getEngineStartStatus() {
        return ConditionEvaluator.fromPercentage(engineStartPerformance);
    }

    public ConditionStatus getOilConditionStatus() {
        return ConditionEvaluator.fromPercentage(oilCondition);
    }

    public ConditionStatus getCoolingSystemStatus() {
        return ConditionEvaluator.fromPercentage(coolingSystemCondition);
    }

    public double averageScore() {
        return (
                generalEngineCondition +
                        fluidLeaks +
                        beltsAndHosesCondition +
                        engineStartPerformance +
                        oilCondition +
                        coolingSystemCondition
        ) / 6.0;
    }

    public ConditionStatus getOverallStatus() {
        return ConditionEvaluator.fromPercentage(averageScore());
    }
}
