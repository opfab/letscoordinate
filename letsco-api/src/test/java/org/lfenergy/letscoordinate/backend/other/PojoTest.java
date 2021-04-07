package org.lfenergy.letscoordinate.backend.other;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.payload.TimeserieDataDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.payload.ValidationDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.payload.ValidationMessageDto;
import org.lfenergy.letscoordinate.backend.dto.reporting.RscKpiDto;
import org.lfenergy.letscoordinate.backend.enums.*;
import org.lfenergy.letscoordinate.backend.exception.IgnoreProcessException;
import org.lfenergy.letscoordinate.backend.exception.PositiveTechnicalQualityCheckException;
import org.lfenergy.letscoordinate.backend.model.*;
import org.lfenergy.letscoordinate.backend.model.opfab.ValidationData;
import org.lfenergy.letscoordinate.backend.util.RscKpiFactory;
import org.lfenergy.letscoordinate.common.exception.AuthorizationException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
public class PojoTest {

    @Test
    public void eventMessage() {
        EventMessage eventMessage = EventMessage.builder().build();
        int hashCode = eventMessage.hashCode();

        eventMessage = new EventMessage();
        eventMessage.setId(1L);
        eventMessage.setMessageId("1234567890");
        eventMessage.setNoun("noun");
        eventMessage.setVerb("verb");
        eventMessage.setTimestamp(Instant.now());
        eventMessage.setSource("source");
        eventMessage.setFormat("format");
        eventMessage.setBusinessApplication("businessApplication");
        eventMessage.setMessageType("messageType");
        eventMessage.setMessageTypeName("messageTypeName");
        eventMessage.setBusinessDayFrom(Instant.now());
        eventMessage.setBusinessDayTo(Instant.now());
        eventMessage.setProcessStep("processStep");
        eventMessage.setTimeframe("timeframe");
        eventMessage.setTimeframeNumber(1);
        eventMessage.setSendingUser("sendingUser");
        eventMessage.setFileName("fileName");
        eventMessage.setTso("tso");
        eventMessage.setBiddingZone("biddingZone");
        eventMessage.setTexts(new ArrayList<>());
        eventMessage.setLinks(new ArrayList<>());
        eventMessage.setRscKpis(new ArrayList<>());
        eventMessage.setTimeseries(new ArrayList<>());

        assertNotEquals(hashCode, eventMessage.hashCode());
    }

    @Test
    public void userService () {
        UserService userService = new UserService();

        int hashCode = userService.hashCode();

        userService.setId(1L);
        userService.setServiceCode("SERVICE_A");
        userService.setUser(new User());

        assertNotNull(userService);
        assertNotNull(userService.toString());
        assertNotEquals(hashCode, userService.hashCode());
    }

    @Test
    public void monitoredTask() {
        MonitoredTask monitoredTaskDto = MonitoredTask.builder()
                .task("task")
                .uuid("uuid")
                .startTime(LocalDateTime.of(2019, 1, 15, 0, 0))
                .endTime(LocalDateTime.of(2020, 1, 15, 0, 0))
                .monitoredTaskSteps(Arrays.asList(MonitoredTaskStep.builder()
                        .step("step1")
                        .context("context1")
                        .startTime(LocalDateTime.of(2019, 2, 8, 0, 0))
                        .endTime(LocalDateTime.of(2019, 2, 9, 0, 0))
                        .status("status1")
                        .comment("comment1")
                        .commentDetails("commentDetails1")
                        .build()))
                .build();
        assertNotNull(monitoredTaskDto);
    }

    @Test
    public void rscKpiData() {
        RscKpiData rscKpiData = new RscKpiData();
        int hashCode = rscKpiData.hashCode();
        rscKpiData = RscKpiFactory.createRscKpiData();
        assertNotNull(rscKpiData);
        assertNotNull(rscKpiData.toString());
        assertNotEquals(hashCode, rscKpiData.hashCode());
    }

    @Test
    public void rscKpiDto() {
        RscKpiDto rscKpiDto = RscKpiDto.builder().build();
        assertNotNull(rscKpiDto.toString());
    }

    @Test
    public void timeserieDataDto() {
        TimeserieDataDto timeserieDataDto = new TimeserieDataDto();
        assertNotNull(timeserieDataDto.toString());
    }

    @Test
    public void timeserie() {
        Timeserie timeserie = Timeserie.builder()
                .id(1L)
                .name("name")
                .eventMessage(new EventMessage())
                .coordinationStatus("coordinationStatus")
                .timeserieDatas(Arrays.asList(
                        TimeserieData.builder()
                                .id(1L)
                                .timestamp(OffsetDateTime.now())
                                .timeserie(new Timeserie())
                                .timeserieDataDetailses(Arrays.asList(new TimeserieDataDetails()))
                                .build()
                ))
                .build();
        Timeserie timeserie2 = new Timeserie(null, null, null, null, null);

        assertNotNull(timeserie.toString());
        assertNotNull(timeserie2.toString());
        assertNotEquals(timeserie2.hashCode(), timeserie.hashCode());
    }

    @Test
    public void timeserieDataDetails() {
        TimeserieDataDetailsEicCode timeserieDataDetailsEicCode = new TimeserieDataDetailsEicCode();
        timeserieDataDetailsEicCode.setId(1L);
        timeserieDataDetailsEicCode.setEicCode("eicCode");
        timeserieDataDetailsEicCode.setTimeserieDataDetails(new TimeserieDataDetails());
        TimeserieDataDetails timeserieDataDetails = new TimeserieDataDetails();
        timeserieDataDetails.setTimeserieDataDetailsEicCodes(
                Arrays.asList(timeserieDataDetailsEicCode)
        );

        TimeserieDataDetails timeserieDataDetails2 = TimeserieDataDetails.builder()
                .id(1L)
                .timeserieData(TimeserieData.builder().id(1L).build())
                .label("label")
                .value("value")
                .accept(1)
                .reject(1)
                .explanation("explanation")
                .comment("comment")
                .timeserieDataDetailsEicCodes(Arrays.asList(
                        TimeserieDataDetailsEicCode.builder()
                                .id(1L)
                                .eicCode("eicCode")
                                .timeserieDataDetails(new TimeserieDataDetails())
                                .build()
                ))
                .build();
        assertNotNull(timeserieDataDetails);
        assertNotNull(timeserieDataDetails2);
        assertNotEquals(timeserieDataDetails.hashCode(), timeserieDataDetails2.hashCode());
    }

    @Test
    public void validationDto() {
        ValidationDto validationDto = new ValidationDto(null, null, null, null);
        ValidationDto validationDto2 = ValidationDto.builder()
                .result(ValidationSeverityEnum.OK)
                .status(ValidationStatusEnum.ACCEPTED)
                .validationType(ValidationTypeEnum.BUSINESS)
                .validationMessages(Arrays.asList(ValidationMessageDto.builder().build()))
                .build();
        assertNotEquals(validationDto.hashCode(), validationDto2.hashCode());
    }

    @Test
    public void validationMessageDto() {
        ValidationMessageDto validationMessageDto = new ValidationMessageDto();

        int hashCode = validationMessageDto.hashCode();

        validationMessageDto.setCode("code");
        validationMessageDto.setSeverity(ValidationSeverityEnum.ERROR);
        validationMessageDto.setTitle("title");
        validationMessageDto.setMessage("message");
        validationMessageDto.setParams(new HashMap<>());
        validationMessageDto.setBusinessTimestamp(Instant.now());
        validationMessageDto.setSourceDataRef(new HashMap<>());

        assertNotNull(validationMessageDto);
        assertFalse(validationMessageDto.getParams().isEmpty());
        assertNotNull(validationMessageDto.getParamsSimple());

        assertNotEquals(hashCode, validationMessageDto.hashCode());
    }

    @Test
    public void ValidationData() {
        ValidationData validationData = new ValidationData();
        validationData.setSendingUser("SendingUser");
        validationData.setTso("TSO");
        validationData.setErrors(Arrays.asList(
                ValidationMessageDto.builder()
                        .code("code")
                        .severity(ValidationSeverityEnum.ERROR)
                        .title("title")
                        .message("message")
                        .params(new HashMap<>())
                        .businessTimestamp(Instant.now())
                        .sourceDataRef(new HashMap<>())
                        .build()
        ));
        validationData.setWarnings(Arrays.asList(
                ValidationMessageDto.builder()
                        .code("code")
                        .severity(ValidationSeverityEnum.ERROR)
                        .title("title")
                        .message("message")
                        .params(new HashMap<>())
                        .businessTimestamp(Instant.now())
                        .sourceDataRef(new HashMap<>())
                        .build()
        ));
        assertNotNull(validationData);
        assertNotNull(validationData.toString());
    }

    @Test
    public void link() {
        Link link = Link.builder()
                .id(1L)
                .name("linkName")
                .value("linkValue")
                .linkEicCodes(Arrays.asList(
                        LinkEicCode.builder()
                                .id(1L)
                                .eicCode("eicCode")
                                .link(new Link())
                                .build()
                ))
                .build();
        String linkAsString = link.toString();
        assertNotNull(linkAsString);
    }

    @Test
    public void text (){
        Text text = new Text();
        text.setId(1L);
        text.setName("name");
        text.setValue("value");
        text.setEventMessage(EventMessage.builder().build());
        String textAsString = text.toString();
        assertNotNull(textAsString);
    }

    @Test
    public void kpiDataSubtypeEnum_BP() {
        assertEquals(KpiDataTypeEnum.BP, KpiDataSubtypeEnum.BP1.getKpiDataType());
    }

    @Test
    public void kpiDataSubtypeEnum_GP() {
        assertEquals(KpiDataTypeEnum.GP, KpiDataSubtypeEnum.GP1.getKpiDataType());
    }

    @Test
    public void kpiDataSubtypeEnum_UNKNOWN() {
        assertEquals(KpiDataTypeEnum.UNKNOWN, KpiDataSubtypeEnum.UNKNOWN.getKpiDataType());
    }

    @Test
    public void ignoreProcessException() {
        IgnoreProcessException ignoreProcessException = new IgnoreProcessException();
        assertNotNull(ignoreProcessException);
        assertNull(ignoreProcessException.getMessage());
    }

    @Test
    public void positiveTechnicalQualityCheckException() {
        PositiveTechnicalQualityCheckException ex = new PositiveTechnicalQualityCheckException();
        assertNotNull(ex);
        assertNull(ex.getMessage());
    }

}
