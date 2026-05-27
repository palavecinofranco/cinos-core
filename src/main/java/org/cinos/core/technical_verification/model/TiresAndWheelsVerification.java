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
public class TiresAndWheelsVerification {

    private Double evenWear;
    private Double spareTireCondition;
    private Double tirePressure;
    private Double tireManufactureDate;
    private Double rimCondition;

    public ConditionStatus getEvenWearStatus() {
        return ConditionEvaluator.fromPercentage(evenWear);
    }

    public ConditionStatus getSpareTireStatus() {
        return ConditionEvaluator.fromPercentage(spareTireCondition);
    }

    public ConditionStatus getPressureStatus() {
        return ConditionEvaluator.fromPercentage(tirePressure);
    }

    public ConditionStatus getManufactureDateStatus() {
        return ConditionEvaluator.fromPercentage(tireManufactureDate);
    }

    public ConditionStatus getRimConditionStatus() {
        return ConditionEvaluator.fromPercentage(rimCondition);
    }

    public double averageScore() {
        return (
                evenWear +
                        spareTireCondition +
                        tirePressure +
                        tireManufactureDate +
                        rimCondition
        ) / 5.0;
    }

    public ConditionStatus getOverallStatus() {
        return ConditionEvaluator.fromPercentage(averageScore());
    }
}
