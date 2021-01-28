package org.lfenergy.letscoordinate.backend.component;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.AbstractMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class OpfabPublisherComponent_IntegrationTest {

    @Autowired
    OpfabPublisherComponent opfabPublisherComponent;

    @Test
    public void generatePlaceholderValue_eicToName() {
        String placeholder = "{{sendingUser::eicToName()}}";
        Map.Entry<String, String> entry = new AbstractMap.SimpleEntry(placeholder, null);
        Map<String, Object> bdiMap = Map.of("sendingUser", "10XES-REE------E");
        Map.Entry<String, String> obtainedResult =
                opfabPublisherComponent.generatePlaceholderValue(entry, bdiMap, null);
        Map.Entry<String, String> expectedResult = new AbstractMap.SimpleEntry<>(placeholder, "REE");
        assertEquals(expectedResult, obtainedResult);
    }

    @Test
    public void generatePlaceholderValue_eicToName_eicCodeUnknown() {
        String placeholder = "{{sendingUser::eicToName()}}";
        Map.Entry<String, String> entry = new AbstractMap.SimpleEntry(placeholder, null);
        Map<String, Object> bdiMap = Map.of("sendingUser", "UNKNOWN-EIC-CODE");
        Map.Entry<String, String> obtainedResult =
                opfabPublisherComponent.generatePlaceholderValue(entry, bdiMap, null);
        Map.Entry<String, String> expectedResult = new AbstractMap.SimpleEntry<>(placeholder, "UNKNOWN-EIC-CODE");
        assertEquals(expectedResult, obtainedResult);
    }
}
