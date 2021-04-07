package org.lfenergy.letscoordinate.backend.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.lfenergy.letscoordinate.backend.enums.DataGranularityEnum;
import org.lfenergy.letscoordinate.backend.model.RscKpi;
import org.lfenergy.letscoordinate.backend.model.RscKpiData;
import org.lfenergy.letscoordinate.backend.model.RscKpiDataDetails;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RscKpiFactory {

    public static RscKpi createRscKpi() {
        return RscKpi.builder()
                .id(1L)
                .eventMessage(null)
                .name("GP1")
                .joinGraph(false)
                .rscKpiDatas(Arrays.asList(createRscKpiData()))
                .build();
    }

    public static RscKpiData createRscKpiData() {
        return RscKpiData.builder()
                .id(2L)
                .timestamp(OffsetDateTime.of(2021, 2, 8, 4, 0, 0, 0, ZoneOffset.UTC))
                .granularity(DataGranularityEnum.DAILY)
                .label("Global Perf 1")
                .rscKpiDataDetails(Arrays.asList(RscKpiDataDetails.builder()
                        .id(3L)
                        .eicCode("10XFR-RTE------Q")
                        .value(1L)
                        .build()
                ))
                .rscKpi(RscKpi.builder().id(1L).build())
                .build();
    }

}
