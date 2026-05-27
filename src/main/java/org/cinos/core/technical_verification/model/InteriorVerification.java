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
public class InteriorVerification{
    // Estado general del tapizado y paneles interiores del vehículo (0 a 100%)
    private Double upholsteryAndPanelsCondition;
    // Funcionamiento del aire acondicionado y calefacción (0 a 100%)
    private Double acAndHeaterFunctionality;
    // Funcionamiento de los levantavidrios eléctricos o manuales (0 a 100%)
    private Double windowMechanismsFunctionality;
    // Funcionamiento del cierre centralizado de puertas (0 a 100%)
    private Double centralLockingFunctionality;
    // Estado general de los cinturones de seguridad y sus anclajes (0 a 100%)
    private Double seatBeltsAndAnchorsCondition;
    // Funcionamiento del sistema de sonido o multimedia (0 a 100%)
    private Double multimediaSystemFunctionality;
    // Validaciones de rango si querés mantenerlas acá

    public ConditionStatus getUpholsteryStatus() {
        return ConditionEvaluator.fromPercentage(upholsteryAndPanelsCondition);
    }

    public ConditionStatus getAcAndHeaterStatus() {
        return ConditionEvaluator.fromPercentage(acAndHeaterFunctionality);
    }

    public ConditionStatus getWindowMechanismsStatus() {
        return ConditionEvaluator.fromPercentage(windowMechanismsFunctionality);
    }

    public ConditionStatus getCentralLockingStatus() {
        return ConditionEvaluator.fromPercentage(centralLockingFunctionality);
    }


    public ConditionStatus getSeatBeltsAndAnchorsStatus() {
        return ConditionEvaluator.fromPercentage(seatBeltsAndAnchorsCondition);
    }

    public ConditionStatus getMultimediaSystemStatus() {
        return ConditionEvaluator.fromPercentage(multimediaSystemFunctionality);
    }

    public double averageScore() {
        return (
                upholsteryAndPanelsCondition +
                        acAndHeaterFunctionality +
                        windowMechanismsFunctionality +
                        centralLockingFunctionality +
                        seatBeltsAndAnchorsCondition +
                        multimediaSystemFunctionality
        ) / 6.0;
    }

    public ConditionStatus getOverallStatus() {
        return ConditionEvaluator.fromPercentage(averageScore());
    }
}

