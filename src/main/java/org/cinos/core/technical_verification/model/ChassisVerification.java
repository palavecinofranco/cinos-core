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
public class ChassisVerification {

    private Double visualDamageInspection;
    private Double rustPresence;
    private Double doorAndHoodAlignment;
    private Double bumperCondition;
    private Double chassisLeaks;

    public ConditionStatus getVisualDamageStatus() {
        return ConditionEvaluator.fromPercentage(visualDamageInspection);
    }

    public ConditionStatus getRustStatus() {
        return ConditionEvaluator.fromPercentage(rustPresence);
    }

    public ConditionStatus getAlignmentStatus() {
        return ConditionEvaluator.fromPercentage(doorAndHoodAlignment);
    }

    public ConditionStatus getBumperStatus() {
        return ConditionEvaluator.fromPercentage(bumperCondition);
    }

    public ConditionStatus getChassisLeaksStatus() {
        return ConditionEvaluator.fromPercentage(chassisLeaks);
    }

    public double averageScore() {
        return (
                visualDamageInspection +
                        rustPresence +
                        doorAndHoodAlignment +
                        bumperCondition +
                        chassisLeaks
        ) / 5.0;
    }

    public ConditionStatus getOverallStatus() {
        return ConditionEvaluator.fromPercentage(averageScore());
    }
}
