package org.lfenergy.letscoordinate.backend.enums;

import lombok.Getter;
import org.lfenergy.operatorfabric.cards.model.SeverityEnum;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.lfenergy.operatorfabric.cards.model.SeverityEnum.*;

@Getter
public enum BasicGenericNounEnum {

    PROCESS_SUCCESSFUL("ProcessSuccessful", "process successful", INFORMATION),
    PROCESS_FAILED("ProcessFailed", "process failed", ALARM),
    PROCESS_ACTION("ProcessAction", "process action", ACTION),
    PROCESS_INFORMATION("ProcessInformation", "process information", INFORMATION),
    MESSAGE_VALIDATED("DfgMessageValidated", "message validated", null);

    private String noun;
    private String titleProcessType;
    private SeverityEnum severity;

    BasicGenericNounEnum(String noun, String titleProcessType, SeverityEnum severity) {
        this.noun = noun;
        this.titleProcessType = titleProcessType;
        this.severity = severity;
    }

    public static BasicGenericNounEnum getByNoun(String noun) {
        return Arrays.stream(BasicGenericNounEnum.values()).filter(b -> b.getNoun().equals(noun)).findFirst()
                .orElse(null);
    }
}
