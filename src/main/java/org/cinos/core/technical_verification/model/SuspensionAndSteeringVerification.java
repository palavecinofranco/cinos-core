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
public class SuspensionAndSteeringVerification {

    private Double shockAbsorbers;
    private Double bushingsAndBallJoints;
    private Double stabilizerBars;
    private Double steeringPlay;
    private Double noiseWhileTurning;

    public ConditionStatus getShockAbsorbersStatus() {
        return ConditionEvaluator.fromPercentage(shockAbsorbers);
    }

    public ConditionStatus getBushingsStatus() {
        return ConditionEvaluator.fromPercentage(bushingsAndBallJoints);
    }

    public ConditionStatus getStabilizerBarsStatus() {
        return ConditionEvaluator.fromPercentage(stabilizerBars);
    }

    public ConditionStatus getSteeringPlayStatus() {
        return ConditionEvaluator.fromPercentage(steeringPlay);
    }

    public ConditionStatus getNoiseStatus() {
        return ConditionEvaluator.fromPercentage(noiseWhileTurning);
    }

    public double averageScore() {
        return (
                shockAbsorbers +
                        bushingsAndBallJoints +
                        stabilizerBars +
                        steeringPlay +
                        noiseWhileTurning
        ) / 5.0;
    }

    public ConditionStatus getOverallStatus() {
        return ConditionEvaluator.fromPercentage(averageScore());
    }
}
