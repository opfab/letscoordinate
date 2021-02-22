package org.lfenergy.letscoordinate.backend.kafka;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lfenergy.letscoordinate.backend.component.OpfabPublisherComponent;
import org.lfenergy.letscoordinate.backend.config.LetscoProperties;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.EventMessageDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.header.BusinessDataIdentifierDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.header.HeaderDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.header.PropertiesDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.payload.PayloadDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.payload.ValidationDto;
import org.lfenergy.letscoordinate.backend.enums.ValidationSeverityEnum;
import org.lfenergy.letscoordinate.backend.enums.ValidationTypeEnum;
import org.lfenergy.letscoordinate.backend.exception.IgnoreProcessException;
import org.lfenergy.letscoordinate.backend.exception.JsonDataMandatoryFieldNullException;
import org.lfenergy.letscoordinate.backend.exception.PositiveTechnicalQualityCheckException;
import org.lfenergy.letscoordinate.backend.mapper.EventMessageMapper;
import org.lfenergy.letscoordinate.backend.model.EventMessage;
import org.lfenergy.letscoordinate.backend.processor.JsonDataProcessor;
import org.lfenergy.letscoordinate.backend.repository.EventMessageRepository;
import org.lfenergy.letscoordinate.backend.util.OpfabUtil;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.lfenergy.letscoordinate.backend.enums.ChangeJsonDataFromWhichEnum.BUSINESS_DATA_IDENTIFIER;
import static org.lfenergy.letscoordinate.backend.enums.ChangeJsonDataFromWhichEnum.HEADER;
import static org.lfenergy.letscoordinate.backend.util.Constants.MESSAGE_VALIDATED;
import static org.lfenergy.letscoordinate.backend.util.Constants.PROCESS_SUCCESSFUL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LetscoKafkaListenerTest {

    private LetscoKafkaListener letscoKafkaListener;

    @Mock
    JsonDataProcessor jsonDataProcessor;
    @Mock
    EventMessageRepository eventMessageRepository;
    @Mock
    OpfabPublisherComponent opfabPublisherComponent;
    LetscoProperties letscoProperties;
    EventMessageDto eventMessageDto;
    Instant timestamp;

    @BeforeEach
    public void beforeEach() {
        letscoProperties = new LetscoProperties();
        LetscoProperties.InputFile inputFile = new LetscoProperties.InputFile();
        LetscoProperties.InputFile.Validation validation = new LetscoProperties.InputFile.Validation();
        inputFile.setValidation(validation);
        letscoProperties.setInputFile(inputFile);
        letscoKafkaListener = new LetscoKafkaListener(jsonDataProcessor, eventMessageRepository,
                opfabPublisherComponent, letscoProperties);
        timestamp = Instant.parse("2021-03-17T10:15:30.00Z");
        eventMessageDto = EventMessageDto.builder()
                .header(HeaderDto.builder()
                        .noun(PROCESS_SUCCESSFUL)
                        .source("source")
                        .messageId("messageId")
                        .timestamp(timestamp)
                        .properties(PropertiesDto.builder()
                                .businessDataIdentifier(BusinessDataIdentifierDto.builder()
                                        .businessApplication("businessApplication")
                                        .messageTypeName("messageTypeName")
                                        .businessDayFrom(Instant.now())
                                        .build()).build()).build()).build();
    }

    @Test
    public void handleLetscoData() throws Exception {
        EventMessage eventMessage = EventMessageMapper.fromDto(eventMessageDto);
        eventMessage.setId(1L);
        when(eventMessageRepository.save(any())).thenReturn(eventMessage);
        doNothing().when(opfabPublisherComponent).publishOpfabCard(eventMessageDto, eventMessage.getId());
        letscoKafkaListener.handleLetscoEventMessages("", 0, "", 0L);
    }

    @Test
    public void verifyData() {
        letscoKafkaListener.verifyData(eventMessageDto);
    }

    @Test
    public void changeSourceIfNeeded_NoChanges() {
        letscoKafkaListener.changeSourceIfNeeded(eventMessageDto);
        assertEquals("source", eventMessageDto.getHeader().getSource());
    }

    @Test
    public void changeSourceIfNeeded_FromBusinessDataIdentifier() {
        Map<String, LetscoProperties.InputFile.ChangeSource> changeSourceMap = Map.of("source",
                new LetscoProperties.InputFile.ChangeSource(BUSINESS_DATA_IDENTIFIER, "businessApplication"));
        letscoProperties.getInputFile().getValidation().setChangeSource(changeSourceMap);
        letscoKafkaListener.changeSourceIfNeeded(eventMessageDto);
        assertEquals("businessApplication", eventMessageDto.getHeader().getSource());
    }

    @Test
    public void changeSourceIfNeeded_FromHeader() {
        Map<String, LetscoProperties.InputFile.ChangeSource> changeSourceMap = Map.of("source",
                new LetscoProperties.InputFile.ChangeSource(HEADER, "messageId"));
        letscoProperties.getInputFile().getValidation().setChangeSource(changeSourceMap);
        letscoKafkaListener.changeSourceIfNeeded(eventMessageDto);
        assertEquals("messageId", eventMessageDto.getHeader().getSource());
    }

    @Test
    public void changeMessageTypeNameIfNeeded() {
        BusinessDataIdentifierDto bdi = eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier();
        Map<String, String> changeMessageTypeNameMap = Map.of("messageTypeName", "newMessageTypeName");
        letscoProperties.getInputFile().getValidation().setChangeMessageTypeName(changeMessageTypeNameMap);
        letscoKafkaListener.changeMessageTypeNameIfNeeded(bdi);
        assertEquals("newMessageTypeName", bdi.getMessageTypeName());
    }

    @Test
    public void changeMessageTypeNameIfNeeded_NoChange() {
        BusinessDataIdentifierDto bdi = eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier();
        letscoKafkaListener.changeMessageTypeNameIfNeeded(bdi);
        assertEquals("messageTypeName", bdi.getMessageTypeName());
    }

    @Test
    public void ignoreProcessIfNeeded() {
        String process = OpfabUtil.generateProcessKey(eventMessageDto);
        List<String> ignoreProcesses = List.of(process);
        letscoProperties.getInputFile().getValidation().setIgnoreProcesses(ignoreProcesses);
        assertThrows(IgnoreProcessException.class, () ->
                letscoKafkaListener.ignoreProcessIfNeeded(process));
    }

    @Test
    public void ignoreProcessIfNeeded_ProcessNotIgnored() {
        String process = OpfabUtil.generateProcessKey(eventMessageDto);
        letscoKafkaListener.ignoreProcessIfNeeded(process);
    }

    @Test
    public void ignoreMessageTypeNameIfNeeded() {
        String messageTypeName =
                eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier().getMessageTypeName();
        List<String> ignoreMessageTypeNames = List.of(messageTypeName);
        letscoProperties.getInputFile().getValidation().setIgnoreMessageTypeNames(ignoreMessageTypeNames);
        assertThrows(IgnoreProcessException.class, () ->
                letscoKafkaListener.ignoreMessageTypeNameIfNeeded(messageTypeName));
    }

    @Test
    public void ignoreMessageTypeNameIfNeeded_MessageTypeNameNotIgnored() {
        letscoKafkaListener.ignoreProcessIfNeeded(
                eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier().getMessageTypeName());
    }

    @Test
    public void ignorePositiveTechnicalQualityCheck() {
        eventMessageDto.getHeader().setNoun(MESSAGE_VALIDATED);
        eventMessageDto.setPayload(PayloadDto.builder()
                .validation(ValidationDto.builder()
                        .result(ValidationSeverityEnum.OK)
                        .validationType(ValidationTypeEnum.TECHNICAL).build()).build());
        assertThrows(PositiveTechnicalQualityCheckException.class, () ->
                letscoKafkaListener.ignorePositiveTechnicalQualityCheck(eventMessageDto));
    }

    @Test
    public void ignorePositiveTechnicalQualityCheck_NotQualityCheck() {
        letscoKafkaListener.ignorePositiveTechnicalQualityCheck(eventMessageDto);
    }

    @Test
    public void ignorePositiveTechnicalQualityCheck_QualityCheckNotOk() {
        eventMessageDto.getHeader().setNoun(MESSAGE_VALIDATED);
        eventMessageDto.setPayload(PayloadDto.builder()
                .validation(ValidationDto.builder()
                        .result(ValidationSeverityEnum.ERROR)
                        .validationType(ValidationTypeEnum.TECHNICAL).build()).build());
        letscoKafkaListener.ignorePositiveTechnicalQualityCheck(eventMessageDto);
    }

    @Test
    public void ignorePositiveTechnicalQualityCheck_QualityCheckOkNotTechnical() {
        eventMessageDto.getHeader().setNoun(MESSAGE_VALIDATED);
        eventMessageDto.setPayload(PayloadDto.builder()
                .validation(ValidationDto.builder()
                        .result(ValidationSeverityEnum.OK)
                        .validationType(ValidationTypeEnum.BUSINESS).build()).build());
        letscoKafkaListener.ignorePositiveTechnicalQualityCheck(eventMessageDto);
    }

    @Test
    public void processIfBusinessDayFromOptional() {
        letscoProperties.getInputFile().getValidation().setBusinessDayFromOptional(true);
        eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier().setBusinessDayFrom(null);
        letscoKafkaListener.processIfBusinessDayFromOptional(eventMessageDto);
        assertEquals(timestamp,
                eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier().getBusinessDayFrom());
    }

    @Test
    public void processIfBusinessDayFromOptional_BdfNotOptional_BdfNull() {
        eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier().setBusinessDayFrom(null);
        Exception exception = assertThrows(JsonDataMandatoryFieldNullException.class,
                () -> letscoKafkaListener.processIfBusinessDayFromOptional(eventMessageDto));
        assertTrue(exception.getMessage().contains("The mandatory field businessDayFrom is null"));
    }

    @Test
    public void processIfBusinessDayFromOptional_BdfNotOptional_BdfNotNull() {
        letscoKafkaListener.processIfBusinessDayFromOptional(eventMessageDto);
    }
}
