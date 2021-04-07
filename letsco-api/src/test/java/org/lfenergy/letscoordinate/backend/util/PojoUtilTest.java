package org.lfenergy.letscoordinate.backend.util;

import lombok.Data;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
public class PojoUtilTest {

    @Test
    public void setProperty_nullInputs() throws NoSuchFieldException, IllegalAccessException {
        PojoForTest pojoForTest = new PojoForTest();
        int hashCode = pojoForTest.hashCode();

        PojoUtil.setProperty(null, null, null);
        assertEquals(hashCode, pojoForTest.hashCode());

        PojoUtil.setProperty(pojoForTest, null, null);
        assertEquals(hashCode, pojoForTest.hashCode());

        PojoUtil.setProperty(pojoForTest, "stringField", null);
        assertEquals(hashCode, pojoForTest.hashCode());
    }

    @Test
    public void setProperty_characterField() throws NoSuchFieldException, IllegalAccessException {
        PojoForTest pojoForTest = new PojoForTest();
        int hashCode = pojoForTest.hashCode();

        PojoUtil.setProperty(pojoForTest, "characterField", "W");
        assertNotEquals(hashCode, pojoForTest.hashCode());
        assertEquals("W", pojoForTest.getCharacterField().toString());
    }

    @Test
    public void setProperty_shortField() throws NoSuchFieldException, IllegalAccessException {
        PojoForTest pojoForTest = new PojoForTest();
        int hashCode = pojoForTest.hashCode();

        PojoUtil.setProperty(pojoForTest, "shortField", "19");
        assertNotEquals(hashCode, pojoForTest.hashCode());
        assertEquals("19", pojoForTest.getShortField().toString());
    }

    @Test
    public void setProperty_integerField() throws NoSuchFieldException, IllegalAccessException {
        PojoForTest pojoForTest = new PojoForTest();
        int hashCode = pojoForTest.hashCode();

        PojoUtil.setProperty(pojoForTest, "integerField", "5");
        assertNotEquals(hashCode, pojoForTest.hashCode());
        assertEquals("5", pojoForTest.getIntegerField().toString());
    }

    @Test
    public void setProperty_longField() throws NoSuchFieldException, IllegalAccessException {
        PojoForTest pojoForTest = new PojoForTest();
        int hashCode = pojoForTest.hashCode();

        PojoUtil.setProperty(pojoForTest, "longField", "1989");
        assertNotEquals(hashCode, pojoForTest.hashCode());
        assertEquals("1989", pojoForTest.getLongField().toString());
    }

    @Test
    public void setProperty_doubleField() throws NoSuchFieldException, IllegalAccessException {
        PojoForTest pojoForTest = new PojoForTest();
        int hashCode = pojoForTest.hashCode();

        PojoUtil.setProperty(pojoForTest, "doubleField", "19.56");
        assertNotEquals(hashCode, pojoForTest.hashCode());
        assertEquals("19.56", pojoForTest.getDoubleField().toString());
    }

    @Test
    public void setProperty_byteField() throws NoSuchFieldException, IllegalAccessException {
        PojoForTest pojoForTest = new PojoForTest();
        int hashCode = pojoForTest.hashCode();

        PojoUtil.setProperty(pojoForTest, "byteField", "1");
        assertNotEquals(hashCode, pojoForTest.hashCode());
        assertEquals("1", pojoForTest.getByteField().toString());
    }

    @Test
    public void setProperty_booleanField() throws NoSuchFieldException, IllegalAccessException {
        PojoForTest pojoForTest = new PojoForTest();
        int hashCode = pojoForTest.hashCode();

        PojoUtil.setProperty(pojoForTest, "booleanField", "true");
        assertNotEquals(hashCode, pojoForTest.hashCode());
        assertEquals("true", pojoForTest.getBooleanField().toString());
    }

    @Test
    public void setProperty_stringField() throws NoSuchFieldException, IllegalAccessException {
        PojoForTest pojoForTest = new PojoForTest();
        int hashCode = pojoForTest.hashCode();

        PojoUtil.setProperty(pojoForTest, "stringField", "LetsCo");
        assertNotEquals(hashCode, pojoForTest.hashCode());
        assertEquals("LetsCo", pojoForTest.getStringField());
    }

    @Test
    public void setProperty_offsetDateTimeField() throws NoSuchFieldException, IllegalAccessException {
        PojoForTest pojoForTest = new PojoForTest();
        int hashCode = pojoForTest.hashCode();

        PojoUtil.setProperty(pojoForTest, "offsetDateTimeField", "2021-04-09T09:24:00Z");
        assertNotEquals(hashCode, pojoForTest.hashCode());
        assertEquals(DateUtil.toOffsetDateTime("2021-04-09T09:24:00Z"), pojoForTest.getOffsetDateTimeField());
    }

    @Test
    public void setProperty_instantField() throws NoSuchFieldException, IllegalAccessException {
        PojoForTest pojoForTest = new PojoForTest();
        int hashCode = pojoForTest.hashCode();

        PojoUtil.setProperty(pojoForTest, "instantField", "2021-04-09T09:24:00Z");
        assertNotEquals(hashCode, pojoForTest.hashCode());
        assertEquals(DateUtil.toOffsetDateTime("2021-04-09T09:24:00Z").toInstant(), pojoForTest.getInstantField());
    }

    @Test
    public void setProperty_listField() throws NoSuchFieldException, IllegalAccessException {
        PojoForTest pojoForTest = new PojoForTest();
        int hashCode = pojoForTest.hashCode();

        PojoUtil.setProperty(pojoForTest, "listField", "1;2;3");
        assertNotEquals(hashCode, pojoForTest.hashCode());
        assertEquals(3, pojoForTest.getListField().size());
    }

    @Test
    public void setProperty_invalidDate() throws NoSuchFieldException, IllegalAccessException {
        PojoForTest pojoForTest = new PojoForTest();
        assertThrows(
                RuntimeException.class,
                () -> PojoUtil.setProperty(pojoForTest, "offsetDateTimeField", "2021-04/09 09:A4:007")
        );
    }

    @Data
    private class PojoForTest {
        private Character characterField;
        private Short shortField;
        private Integer integerField;
        private Long longField;
        private Double doubleField;
        private Byte byteField;
        private Boolean booleanField;
        private String stringField;
        private OffsetDateTime offsetDateTimeField;
        private Instant instantField;
        private List listField;
    }

}
