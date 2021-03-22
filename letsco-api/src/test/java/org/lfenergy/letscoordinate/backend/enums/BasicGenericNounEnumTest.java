package org.lfenergy.letscoordinate.backend.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.lfenergy.letscoordinate.backend.enums.BasicGenericNounEnum.*;

public class BasicGenericNounEnumTest {

    @Test
    public void getByNoun_ProcessSuccessful() {
        BasicGenericNounEnum basicGenericNoun = BasicGenericNounEnum.getByNoun("ProcessSuccessful");
        assertEquals(PROCESS_SUCCESSFUL, basicGenericNoun);
    }

    @Test
    public void getByNoun_ProcessFailed() {
        BasicGenericNounEnum basicGenericNoun = BasicGenericNounEnum.getByNoun("ProcessFailed");
        assertEquals(PROCESS_FAILED, basicGenericNoun);
    }

    @Test
    public void getByNoun_ProcessAction() {
        BasicGenericNounEnum basicGenericNoun = BasicGenericNounEnum.getByNoun("ProcessAction");
        assertEquals(PROCESS_ACTION, basicGenericNoun);
    }

    @Test
    public void getByNoun_ProcessInformation() {
        BasicGenericNounEnum basicGenericNoun = BasicGenericNounEnum.getByNoun("ProcessInformation");
        assertEquals(PROCESS_INFORMATION, basicGenericNoun);
    }

    @Test
    public void getByNoun_MessageValidated() {
        BasicGenericNounEnum basicGenericNoun = BasicGenericNounEnum.getByNoun("DfgMessageValidated");
        assertEquals(MESSAGE_VALIDATED, basicGenericNoun);
    }

    @Test
    public void getByNoun_NotRecognizedNoun() {
        BasicGenericNounEnum basicGenericNoun = BasicGenericNounEnum.getByNoun("UnknownNoun");
        assertNull(basicGenericNoun);
    }
}
