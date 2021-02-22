package org.lfenergy.letscoordinate.backend.util;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.OffsetDateTime;

@RunWith(SpringRunner.class)
public class DateUtilTest {

    @Test
    public void isValidJsonDate_shouldReturnFalse(){
        Assertions.assertThat(DateUtil.isValidJsonDate(null)).isFalse();
        Assertions.assertThat(DateUtil.isValidJsonDate("")).isFalse();
        Assertions.assertThat(DateUtil.isValidJsonDate("2021-01-01T00:00:00.000Z")).isFalse();
        Assertions.assertThat(DateUtil.isValidJsonDate("999-01-01T00:00:00Z")).isFalse();
        Assertions.assertThat(DateUtil.isValidJsonDate("2021-1-01T00:00:00Z")).isFalse();
        Assertions.assertThat(DateUtil.isValidJsonDate("2021-99-01T00:00:00Z")).isFalse();
        Assertions.assertThat(DateUtil.isValidJsonDate("2021-01-1T00:00:00Z")).isFalse();
        Assertions.assertThat(DateUtil.isValidJsonDate("2021-01-99T00:00:00Z")).isFalse();
        Assertions.assertThat(DateUtil.isValidJsonDate("2021-01-01T0:00:00Z")).isFalse();
        Assertions.assertThat(DateUtil.isValidJsonDate("2021-01-01T99:00:00Z")).isFalse();
        Assertions.assertThat(DateUtil.isValidJsonDate("2021-01-01T00:0:00Z")).isFalse();
        Assertions.assertThat(DateUtil.isValidJsonDate("2021-01-01T00:99:00Z")).isFalse();
        Assertions.assertThat(DateUtil.isValidJsonDate("2021-01-01T00:00:0Z")).isFalse();
        Assertions.assertThat(DateUtil.isValidJsonDate("2021-01-01T00:00:99Z")).isFalse();
    }

    @Test
    public void isValidJsonDate_shouldReturnTrue() {
        Assertions.assertThat(DateUtil.isValidJsonDate("2021-01-01T00:00:00Z")).isTrue();
        Assertions.assertThat(DateUtil.isValidJsonDate("9999-12-31T23:59:59Z")).isTrue();
    }

}
