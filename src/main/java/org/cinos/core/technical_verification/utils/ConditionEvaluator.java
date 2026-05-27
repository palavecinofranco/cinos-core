package org.cinos.core.technical_verification.utils;

import org.cinos.core.technical_verification.model.ConditionStatus;

public class ConditionEvaluator {

    public static ConditionStatus fromPercentage(double percentage) {
        if (percentage >= 90.0) {
            return ConditionStatus.OK;
        } else if (percentage >= 60.0) {
            return ConditionStatus.OBS;
        } else {
            return ConditionStatus.BAD;
        }
    }
}
