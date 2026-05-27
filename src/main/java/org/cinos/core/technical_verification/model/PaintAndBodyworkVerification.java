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
public class PaintAndBodyworkVerification {

    private Double toneDifferences;
    private Double puttyOrTouchUps;
    private Double dentsOrHits;
    private Double exteriorPlastics;
    private Double glassCondition;

    public ConditionStatus getToneDifferencesStatus() {
        return ConditionEvaluator.fromPercentage(toneDifferences);
    }

    public ConditionStatus getTouchUpsStatus() {
        return ConditionEvaluator.fromPercentage(puttyOrTouchUps);
    }

    public ConditionStatus getDentsStatus() {
        return ConditionEvaluator.fromPercentage(dentsOrHits);
    }

    public ConditionStatus getPlasticsStatus() {
        return ConditionEvaluator.fromPercentage(exteriorPlastics);
    }

    public ConditionStatus getGlassStatus() {
        return ConditionEvaluator.fromPercentage(glassCondition);
    }

    public double averageScore() {
        return (
                toneDifferences +
                        puttyOrTouchUps +
                        dentsOrHits +
                        exteriorPlastics +
                        glassCondition
        ) / 5.0;
    }

    public ConditionStatus getOverallStatus() {
        return ConditionEvaluator.fromPercentage(averageScore());
    }
}
