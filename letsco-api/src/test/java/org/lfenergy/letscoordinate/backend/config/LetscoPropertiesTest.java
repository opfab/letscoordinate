package org.lfenergy.letscoordinate.backend.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lfenergy.letscoordinate.backend.enums.BasicGenericNounEnum;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class LetscoPropertiesTest {

    LetscoProperties letscoProperties;

    @BeforeEach
    public void beforeEach() {
        letscoProperties = new LetscoProperties();
        LetscoProperties.InputFile inputFile = new LetscoProperties.InputFile();
        inputFile.setGenericNouns(Map.of(
                BasicGenericNounEnum.PROCESS_SUCCESSFUL, List.of("otherPS_1", "otherPS_2"),
                BasicGenericNounEnum.PROCESS_FAILED, List.of("otherPF_1")));
        letscoProperties.setInputFile(inputFile);
    }

    @Test
    public void allGenericNouns() {
        assertEquals(
                List.of("ProcessSuccessful", "ProcessFailed", "otherPS_1", "otherPS_2", "otherPF_1", "ProcessAction",
                        "ProcessInformation", "DfgMessageValidated").stream().sorted().collect(toList()),
                letscoProperties.getInputFile().allGenericNouns().stream().sorted().collect(toList()));
    }
}
