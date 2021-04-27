/*
 * Copyright (c) 2020-2021, RTE (https://www.rte-france.com)
 * Copyright (c) 2020-2021 RTE international (https://www.rte-international.com)
 * See AUTHORS.txt
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 * This file is part of the Let’s Coordinate project.
 */

package org.lfenergy.letscoordinate.backend.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.vavr.control.Validation;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lfenergy.letscoordinate.backend.config.CoordinationConfig;
import org.lfenergy.letscoordinate.backend.config.LetscoProperties;
import org.lfenergy.letscoordinate.backend.dto.ResponseErrorDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.EventMessageDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.EventMessageWrapperDto;
import org.lfenergy.letscoordinate.backend.dto.reporting.RscKpiDto;
import org.lfenergy.letscoordinate.backend.dto.reporting.RscKpiReportDataDto;
import org.lfenergy.letscoordinate.backend.dto.reporting.RscKpiReportSubmittedFormDataDto;
import org.lfenergy.letscoordinate.backend.enums.DataGranularityEnum;
import org.lfenergy.letscoordinate.backend.enums.ExcelBlocEnum;
import org.lfenergy.letscoordinate.backend.enums.KpiDataTypeEnum;
import org.lfenergy.letscoordinate.backend.exception.InvalidInputFileException;
import org.lfenergy.letscoordinate.backend.service.EventMessageService;
import org.lfenergy.letscoordinate.backend.util.ApplicationContextUtil;
import org.lfenergy.letscoordinate.backend.util.Constants;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExcelDataProcessorTest {

    ExcelDataProcessor excelDataProcessor;
    LetscoProperties letscoProperties;
    CoordinationConfig coordinationConfig;
    EventMessageService eventMessageService;
    MockMultipartFile validMultipartFile;
    MockMultipartFile validMultipartFileWithLowercaseTitles;
    MockMultipartFile invalidMultipartFile;

    @BeforeEach
    public void before() throws IOException {
        letscoProperties = new LetscoProperties();
        LetscoProperties.InputFile inputFile = new LetscoProperties.InputFile();
        LetscoProperties.InputFile.Validation validation = new LetscoProperties.InputFile.Validation();
        validation.setAcceptPropertiesIgnoreCase(true);
        validation.setFailOnUnknownProperties(false);
        inputFile.setValidation(validation);
        letscoProperties.setInputFile(inputFile);

        coordinationConfig = ApplicationContextUtil.initCoordinationConfig();

        eventMessageService = new EventMessageService(coordinationConfig, letscoProperties);

        excelDataProcessor = new ExcelDataProcessor(letscoProperties, coordinationConfig, eventMessageService);

        File file1 = new File("src/test/resources/validTestFile_1.xlsx");
        validMultipartFile = new MockMultipartFile("file", file1.getName(), null, new FileInputStream(file1));

        File file2 = new File("src/test/resources/validTestFileWithLowercaseTitles.xlsx");
        validMultipartFileWithLowercaseTitles = new MockMultipartFile("file", file2.getName(), null, new FileInputStream(file2));

        File file3 = new File("src/test/resources/invalidTestFile_1.xlsx");
        invalidMultipartFile = new MockMultipartFile("file", file3.getName(), null, new FileInputStream(file3));
    }

    @Test
    public void inputStreamToPojo_shouldReturnEventMessageDtoWhenInputFileIsValid()
            throws IOException, NoSuchFieldException, IllegalAccessException, InstantiationException, InvalidInputFileException {
        Validation<ResponseErrorDto, EventMessageDto> outputValidation = excelDataProcessor.inputStreamToPojo("", validMultipartFile.getInputStream());
        assertAll(
                () -> assertEquals(true, outputValidation.isValid()),
                () -> assertNotNull(outputValidation.get()),
                () -> assertNotNull(outputValidation.get().getHeader()),
                () -> assertEquals("b071aa50097f49f1bd69e82a070084b6", outputValidation.get().getHeader().getMessageId())
        );
    }

    @Test
    public void inputStreamToPojo_shouldReturnResponseErrorDtoWhenInputFileIsInvalid()
            throws IOException, NoSuchFieldException, IllegalAccessException, InstantiationException, InvalidInputFileException {
        Validation<ResponseErrorDto, EventMessageDto> outputValidation = excelDataProcessor.inputStreamToPojo("", invalidMultipartFile.getInputStream());
        assertAll(
                () -> assertEquals(true, outputValidation.isInvalid()),
                () -> assertNotNull(outputValidation.getError()),
                () -> assertNotNull(outputValidation.getError().getMessages()),
                () -> assertEquals(HttpStatus.BAD_REQUEST.value(), outputValidation.getError().getStatus()),
                () -> assertEquals("INVALID_INPUT_FILE", outputValidation.getError().getCode()),
                () -> assertEquals(2, outputValidation.getError().getMessages().size())
        );
    }

    @Test
    public void validateExcelFileAndMapColTitlesIndexesByBloc_shouldReturnEmptyMapIfWorkbookIsNull() throws InvalidInputFileException {
        Map<ExcelBlocEnum, Map<String, Integer>> map = excelDataProcessor.validateExcelFileAndMapColTitlesIndexesByBloc(null);
        assertAll(
                () -> assertNotNull(map),
                () -> assertTrue(map.isEmpty())
        );
    }

    @Test
    public void validateExcelFileAndMapColTitlesIndexesByBloc_shouldReturnFullMapIfEffectiveDataSheetFound()
            throws IOException, InvalidInputFileException {
        Workbook workbook = new XSSFWorkbook(validMultipartFile.getInputStream());
        Map<ExcelBlocEnum, Map<String, Integer>> map = excelDataProcessor.validateExcelFileAndMapColTitlesIndexesByBloc(workbook);
        assertAll(
                () -> assertNotNull(map),
                () -> assertTrue(!map.isEmpty())
        );
    }

    @Test()
    public void validateExcelFileAndMapColTitlesIndexesByBloc_shouldThrowExceptionIfEffectiveDataSheetNotFound() {
        assertThrows(InvalidInputFileException.class, () -> excelDataProcessor.validateExcelFileAndMapColTitlesIndexesByBloc(new XSSFWorkbook()));
    }

    @Test
    public void getColTitlesIndexesForExcelBloc_shouldReturnFullMap() throws IOException, InvalidInputFileException {
        Workbook workbook = new XSSFWorkbook(validMultipartFile.getInputStream());
        Sheet effectiveDataSheet = workbook.getSheetAt(excelDataProcessor.EFFECTIVE_DATA_SHEET_INDEX);
        Map<String, Integer> map = excelDataProcessor.getColTitlesIndexesForExcelBloc(ExcelBlocEnum.HEADER,
                excelDataProcessor.headerColNames, effectiveDataSheet);
        assertAll(
                () -> assertNotNull(map),
                () -> assertTrue(!map.isEmpty())
        );
    }

    @Test
    public void getColTitlesIndexesForExcelBloc_acceptPropertiesIgnoreCase_true() throws IOException, InvalidInputFileException {
        letscoProperties.getInputFile().getValidation().setAcceptPropertiesIgnoreCase(true);
        Workbook workbook = new XSSFWorkbook(validMultipartFileWithLowercaseTitles.getInputStream());
        Sheet effectiveDataSheet = workbook.getSheetAt(excelDataProcessor.EFFECTIVE_DATA_SHEET_INDEX);
        Map<String, Integer> map = excelDataProcessor.getColTitlesIndexesForExcelBloc(ExcelBlocEnum.HEADER,
                excelDataProcessor.headerColNames, effectiveDataSheet);
        assertAll(
                () -> assertNotNull(map),
                () -> assertTrue(!map.isEmpty()),
                () -> assertEquals(excelDataProcessor.headerColNames.size(), map.keySet().size())
        );
    }

    @Test
    public void getColTitlesIndexesForExcelBloc_acceptPropertiesIgnoreCase_false() throws IOException, InvalidInputFileException {
        letscoProperties.getInputFile().getValidation().setAcceptPropertiesIgnoreCase(false);
        Workbook workbook = new XSSFWorkbook(validMultipartFileWithLowercaseTitles.getInputStream());
        Sheet effectiveDataSheet = workbook.getSheetAt(excelDataProcessor.EFFECTIVE_DATA_SHEET_INDEX);
        Map<String, Integer> map = excelDataProcessor.getColTitlesIndexesForExcelBloc(ExcelBlocEnum.HEADER,
                excelDataProcessor.headerColNames, effectiveDataSheet);
        assertAll(
                () -> assertNotNull(map),
                () -> assertTrue(!map.isEmpty()),
                () -> assertEquals(8, map.keySet().size())
        );
    }

    @Test
    public void getColTitlesIndexesForExcelBloc_shouldThrowExceptionIfTitleRowNotFound() {
        Workbook workbook = new XSSFWorkbook();
        workbook.createSheet();
        assertThrows(InvalidInputFileException.class, () -> excelDataProcessor.getColTitlesIndexesForExcelBloc(ExcelBlocEnum.HEADER,
                excelDataProcessor.headerColNames, workbook.getSheetAt(0)));
    }

    @Test
    public void getColTitlesIndexesForExcelBloc_invalidFile_failOnUnknownProperties_true() throws IOException {
        letscoProperties.getInputFile().getValidation().setFailOnUnknownProperties(true);
        Workbook workbook = new XSSFWorkbook(invalidMultipartFile.getInputStream());
        assertThrows(InvalidInputFileException.class, () -> excelDataProcessor.getColTitlesIndexesForExcelBloc(ExcelBlocEnum.HEADER,
                excelDataProcessor.headerColNames, workbook.getSheetAt(excelDataProcessor.EFFECTIVE_DATA_SHEET_INDEX)));
    }

    @Test
    public void getColTitlesIndexesForExcelBloc_invalidFile_duplicatedColumnTitleFound() throws IOException {
        letscoProperties.getInputFile().getValidation().setFailOnUnknownProperties(false);
        String duplictedHeaderColumnName = "verb";
        Workbook workbook = new XSSFWorkbook(invalidMultipartFile.getInputStream());
        Sheet sheet = workbook.getSheetAt(excelDataProcessor.EFFECTIVE_DATA_SHEET_INDEX);
        Row row = sheet.getRow(ExcelBlocEnum.HEADER.getTitlesRowIndex());
        row.createCell(row.getLastCellNum()+1).setCellValue(duplictedHeaderColumnName);
        assertThrows(InvalidInputFileException.class, () -> excelDataProcessor.getColTitlesIndexesForExcelBloc(ExcelBlocEnum.HEADER,
                excelDataProcessor.headerColNames, sheet));
    }

    @Test
    public void initEventMessageHeader_shouldUpdateEventMessageDtoHeader() throws IOException, InvalidInputFileException, NoSuchFieldException, IllegalAccessException {
        Workbook workbook = new XSSFWorkbook(validMultipartFile.getInputStream());
        Sheet effectiveDataSheet = workbook.getSheetAt(excelDataProcessor.EFFECTIVE_DATA_SHEET_INDEX);
        Map<ExcelBlocEnum, Map<String, Integer>> columnIndexMap = excelDataProcessor.validateExcelFileAndMapColTitlesIndexesByBloc(workbook);
        EventMessageDto eventMessageDto = new EventMessageDto();
        int initialHashCode = eventMessageDto.hashCode();
        excelDataProcessor.initEventMessageHeader(eventMessageDto, effectiveDataSheet, columnIndexMap.get(ExcelBlocEnum.HEADER));
        assertNotEquals(initialHashCode, eventMessageDto.hashCode());
    }

    @Test
    public void initEventMessageHeader_shouldNotUpdateEventMessageDtoHeaderWhenSheetIsNull() throws IOException, InvalidInputFileException, NoSuchFieldException, IllegalAccessException {
        Workbook workbook = new XSSFWorkbook(validMultipartFile.getInputStream());
        Sheet effectiveDataSheet = null;
        Map<ExcelBlocEnum, Map<String, Integer>> columnIndexMap = excelDataProcessor.validateExcelFileAndMapColTitlesIndexesByBloc(workbook);
        EventMessageDto eventMessageDto = new EventMessageDto();
        int initialHashCode = eventMessageDto.hashCode();
        excelDataProcessor.initEventMessageHeader(eventMessageDto, effectiveDataSheet, columnIndexMap.get(ExcelBlocEnum.HEADER));
        assertEquals(initialHashCode, eventMessageDto.hashCode());
    }

    @Test
    public void initEventMessagePayload_shouldUpdateEventMessageDtoPayload() throws IOException, InvalidInputFileException, NoSuchFieldException, IllegalAccessException, InstantiationException {
        Workbook workbook = new XSSFWorkbook(validMultipartFile.getInputStream());
        Sheet effectiveDataSheet = workbook.getSheetAt(excelDataProcessor.EFFECTIVE_DATA_SHEET_INDEX);
        Map<ExcelBlocEnum, Map<String, Integer>> columnIndexMap = excelDataProcessor.validateExcelFileAndMapColTitlesIndexesByBloc(workbook);
        EventMessageDto eventMessageDto = new EventMessageDto();
        int initialHashCode = eventMessageDto.hashCode();
        excelDataProcessor.initEventMessagePayload(eventMessageDto, effectiveDataSheet, columnIndexMap.get(ExcelBlocEnum.PAYLOAD));
        assertNotEquals(initialHashCode, eventMessageDto.hashCode());
    }

    @Test
    public void initEventMessagePayload_shouldNotUpdateEventMessageDtoPayload() throws NoSuchFieldException, IllegalAccessException, InstantiationException, InvalidInputFileException {
        EventMessageDto eventMessageDto = new EventMessageDto();
        int initialHashCode = eventMessageDto.hashCode();
        excelDataProcessor.initEventMessagePayload(eventMessageDto, null, null);
        assertEquals(initialHashCode, eventMessageDto.hashCode());
    }

    /* * * * * * * * * * * * * * * * * * * *\
    |*   GENERATING RSC KPI EXCEL REPORT   *|
    \* * * * * * * * * * * * * * * * * * * */

    @Test
    public void generateRscKpiExcelReport_daily_rsc() throws IOException {
        RscKpiReportDataDto rscKpiReportDataDto = RscKpiReportDataDto.builder().build();
        byte[] bytesWhenEmptyInput = excelDataProcessor.generateRscKpiExcelReport(rscKpiReportDataDto);
        assertNotNull(bytesWhenEmptyInput);

        File file = new File("src/test/resources/rscKpiReportDataDto_daily.json");
        InputStream inputStream = new FileInputStream(file);
        RscKpiReportDataDto dailyRscKpiReportDataDto = new ObjectMapper().registerModule(new JavaTimeModule())
                        .readValue(inputStream, RscKpiReportDataDto.class);
        assertNotNull(dailyRscKpiReportDataDto);

        dailyRscKpiReportDataDto.getSubmittedFormData().setRegionCodes(null);
        // Pan-EU RSC
        dailyRscKpiReportDataDto.getSubmittedFormData().setRscCodes(Arrays.asList(Constants.ALL_RSCS_CODE));
        byte[] bytesWhenDailyAndPanEuRsc = excelDataProcessor.generateRscKpiExcelReport(dailyRscKpiReportDataDto);
        assertNotNull(bytesWhenDailyAndPanEuRsc);
        assertTrue(bytesWhenDailyAndPanEuRsc.length > bytesWhenEmptyInput.length);
        // Not Pan-EU RSC
        dailyRscKpiReportDataDto.getSubmittedFormData().setRscCodes(Arrays.asList("38X-BALTIC-RSC-H"));
        byte[] bytesWhenDailyAndNotPanEuRsc = excelDataProcessor.generateRscKpiExcelReport(dailyRscKpiReportDataDto);
        assertNotNull(bytesWhenDailyAndNotPanEuRsc);
        assertTrue(bytesWhenDailyAndNotPanEuRsc.length > bytesWhenEmptyInput.length);
    }

    @Test
    public void generateRscKpiExcelReport_daily_region() throws IOException {
        RscKpiReportDataDto rscKpiReportDataDto = RscKpiReportDataDto.builder().build();
        byte[] bytesWhenEmptyInput = excelDataProcessor.generateRscKpiExcelReport(rscKpiReportDataDto);
        assertNotNull(bytesWhenEmptyInput);

        File file = new File("src/test/resources/rscKpiReportDataDto_daily.json");
        InputStream inputStream = new FileInputStream(file);
        RscKpiReportDataDto dailyRscKpiReportDataDto = new ObjectMapper().registerModule(new JavaTimeModule())
                        .readValue(inputStream, RscKpiReportDataDto.class);
        assertNotNull(dailyRscKpiReportDataDto);

        dailyRscKpiReportDataDto.getSubmittedFormData().setRscCodes(null);
        // Pan-EU RSC
        dailyRscKpiReportDataDto.getSubmittedFormData().setRegionCodes(Arrays.asList(Constants.ALL_REGIONS_CODE));
        byte[] bytesWhenDailyAndPanEuRegion = excelDataProcessor.generateRscKpiExcelReport(dailyRscKpiReportDataDto);
        assertNotNull(bytesWhenDailyAndPanEuRegion);
        assertTrue(bytesWhenDailyAndPanEuRegion.length > bytesWhenEmptyInput.length);
        // Not Pan-EU RSC
        dailyRscKpiReportDataDto.getSubmittedFormData().setRegionCodes(Arrays.asList("10Y1001C--00120B"));
        byte[] bytesWhenDailyAndNotPanEuRsc = excelDataProcessor.generateRscKpiExcelReport(dailyRscKpiReportDataDto);
        assertNotNull(bytesWhenDailyAndNotPanEuRsc);
        assertTrue(bytesWhenDailyAndNotPanEuRsc.length > bytesWhenEmptyInput.length);
    }

    @Test
    public void generateRscKpiExcelReport_yearly_rsc() throws IOException {
        RscKpiReportDataDto rscKpiReportDataDto = RscKpiReportDataDto.builder().build();
        byte[] bytesWhenEmptyInput = excelDataProcessor.generateRscKpiExcelReport(rscKpiReportDataDto);
        assertNotNull(bytesWhenEmptyInput);

        File file = new File("src/test/resources/rscKpiReportDataDto_yearly.json");
        InputStream inputStream = new FileInputStream(file);
        RscKpiReportDataDto yearlyRscKpiReportDataDto = new ObjectMapper().registerModule(new JavaTimeModule())
                        .readValue(inputStream, RscKpiReportDataDto.class);
        assertNotNull(yearlyRscKpiReportDataDto);

        yearlyRscKpiReportDataDto.getSubmittedFormData().setRegionCodes(null);
        // Pan-EU RSC
        yearlyRscKpiReportDataDto.getSubmittedFormData().setRscCodes(Arrays.asList(Constants.ALL_RSCS_CODE));
        byte[] bytesWhenDailyAndPanEuRsc = excelDataProcessor.generateRscKpiExcelReport(yearlyRscKpiReportDataDto);
        assertNotNull(bytesWhenDailyAndPanEuRsc);
        assertTrue(bytesWhenDailyAndPanEuRsc.length > bytesWhenEmptyInput.length);
        // Not Pan-EU RSC
        yearlyRscKpiReportDataDto.getSubmittedFormData().setRscCodes(Arrays.asList("38X-BALTIC-RSC-H"));
        byte[] bytesWhenDailyAndNotPanEuRsc = excelDataProcessor.generateRscKpiExcelReport(yearlyRscKpiReportDataDto);
        assertNotNull(bytesWhenDailyAndNotPanEuRsc);
        assertTrue(bytesWhenDailyAndNotPanEuRsc.length > bytesWhenEmptyInput.length);
    }

    @Test
    public void generateRscKpiExcelReport_yearly_region() throws IOException {
        RscKpiReportDataDto rscKpiReportDataDto = RscKpiReportDataDto.builder().build();
        byte[] bytesWhenEmptyInput = excelDataProcessor.generateRscKpiExcelReport(rscKpiReportDataDto);
        assertNotNull(bytesWhenEmptyInput);

        File file = new File("src/test/resources/rscKpiReportDataDto_yearly.json");
        InputStream inputStream = new FileInputStream(file);
        RscKpiReportDataDto yearlyRscKpiReportDataDto = new ObjectMapper().registerModule(new JavaTimeModule())
                        .readValue(inputStream, RscKpiReportDataDto.class);
        assertNotNull(yearlyRscKpiReportDataDto);

        yearlyRscKpiReportDataDto.getSubmittedFormData().setRscCodes(null);
        // Pan-EU RSC
        yearlyRscKpiReportDataDto.getSubmittedFormData().setRegionCodes(Arrays.asList(Constants.ALL_REGIONS_CODE));
        byte[] bytesWhenDailyAndPanEuRegion = excelDataProcessor.generateRscKpiExcelReport(yearlyRscKpiReportDataDto);
        assertNotNull(bytesWhenDailyAndPanEuRegion);
        assertTrue(bytesWhenDailyAndPanEuRegion.length > bytesWhenEmptyInput.length);
        // Not Pan-EU RSC
        yearlyRscKpiReportDataDto.getSubmittedFormData().setRegionCodes(Arrays.asList("10Y1001C--00120B"));
        byte[] bytesWhenDailyAndNotPanEuRsc = excelDataProcessor.generateRscKpiExcelReport(yearlyRscKpiReportDataDto);
        assertNotNull(bytesWhenDailyAndNotPanEuRsc);
        assertTrue(bytesWhenDailyAndNotPanEuRsc.length > bytesWhenEmptyInput.length);
    }

    @Test
    public void isAllRscSelected_shouldReturnTrue() {
        RscKpiReportSubmittedFormDataDto submittedFormDataDto = RscKpiReportSubmittedFormDataDto.builder()
                .rscCodes(Arrays.asList(Constants.ALL_RSCS_CODE))
                .build();
        assertTrue(excelDataProcessor.isAllRscSelected(submittedFormDataDto));
    }

    @Test
    public void isAllRscSelected_shouldReturnFalse() {
        RscKpiReportSubmittedFormDataDto submittedFormDataDto = RscKpiReportSubmittedFormDataDto.builder()
                .rscCodes(null)
                .build();
        assertFalse(excelDataProcessor.isAllRscSelected(submittedFormDataDto));

        submittedFormDataDto = RscKpiReportSubmittedFormDataDto.builder()
                .rscCodes(Arrays.asList())
                .build();
        assertFalse(excelDataProcessor.isAllRscSelected(submittedFormDataDto));

        submittedFormDataDto = RscKpiReportSubmittedFormDataDto.builder()
                .rscCodes(Arrays.asList("38X-BALTIC-RSC-H"))
                .build();
        assertFalse(excelDataProcessor.isAllRscSelected(submittedFormDataDto));
    }

    @Test
    public void isAllRegionSelected_shouldReturnTrue() {
        RscKpiReportSubmittedFormDataDto submittedFormDataDto = RscKpiReportSubmittedFormDataDto.builder()
                .regionCodes(Arrays.asList(Constants.ALL_REGIONS_CODE))
                .build();
        assertTrue(excelDataProcessor.isAllRegionSelected(submittedFormDataDto));
    }

    @Test
    public void isAllRegionSelected_shouldReturnFalse() {
        RscKpiReportSubmittedFormDataDto submittedFormDataDto = RscKpiReportSubmittedFormDataDto.builder()
                .regionCodes(null)
                .build();
        assertFalse(excelDataProcessor.isAllRegionSelected(submittedFormDataDto));

        submittedFormDataDto = RscKpiReportSubmittedFormDataDto.builder()
                .regionCodes(Arrays.asList())
                .build();
        assertFalse(excelDataProcessor.isAllRegionSelected(submittedFormDataDto));

        submittedFormDataDto = RscKpiReportSubmittedFormDataDto.builder()
                .regionCodes(Arrays.asList("10Y1001C--00120B"))
                .build();
        assertFalse(excelDataProcessor.isAllRegionSelected(submittedFormDataDto));
    }

    @Test
    public void getDataDetailsByEicCode_emptyInputs() {
        assertTrue(excelDataProcessor.getDataDetailsByEicCode(null, null, null).isEmpty());
        assertTrue(excelDataProcessor.getDataDetailsByEicCode("eicCodeTest", null, null).isEmpty());
        assertTrue(excelDataProcessor.getDataDetailsByEicCode("eicCodeTest", KpiDataTypeEnum.BP, Arrays.asList()).isEmpty());
    }

    @Test
    public void getDataDetailsByEicCode_GP_shouldReturnExactValue() {
        List<RscKpiDto.DataDto> dataDtoList = new ArrayList<>();
        dataDtoList.add(RscKpiDto.DataDto.builder()
                .details(Arrays.asList(
                        RscKpiDto.DataDto.DetailsDto.builder()
                                .value(19L)
                                .eicCode(null)
                                .build(),
                        RscKpiDto.DataDto.DetailsDto.builder()
                                .value(1L)
                                .eicCode("eicCodeTest")
                                .build()
                ))
                .build());
        Optional<RscKpiDto.DataDto.DetailsDto> details = excelDataProcessor.getDataDetailsByEicCode("eicCodeTest", KpiDataTypeEnum.GP, dataDtoList);
        assertFalse(details.isEmpty());
        assertEquals(1L, details.get().getValue());
        assertEquals("eicCodeTest", details.get().getEicCode());
    }

    @Test
    public void getDataDetailsByEicCode_GP_shouldReturnGlobalValue() {
        List<RscKpiDto.DataDto> dataDtoList = new ArrayList<>();
        dataDtoList.add(RscKpiDto.DataDto.builder()
                .details(Arrays.asList(
                        RscKpiDto.DataDto.DetailsDto.builder()
                                .value(19L)
                                .eicCode(null)
                                .build()
                ))
                .build());
        Optional<RscKpiDto.DataDto.DetailsDto> details = excelDataProcessor.getDataDetailsByEicCode("eicCodeTest", KpiDataTypeEnum.GP, dataDtoList);
        assertFalse(details.isEmpty());
        assertEquals(19L, details.get().getValue());
        assertEquals(null, details.get().getEicCode());
    }

    @Test
    public void getDataDetailsByEicCode_GP_shouldReturnEmpty() {
        List<RscKpiDto.DataDto> dataDtoList = new ArrayList<>();
        dataDtoList.add(RscKpiDto.DataDto.builder()
                .details(Arrays.asList(null, null))
                .build());
        Optional<RscKpiDto.DataDto.DetailsDto> details = excelDataProcessor.getDataDetailsByEicCode("eicCodeTest", KpiDataTypeEnum.GP, dataDtoList);
        assertTrue(details.isEmpty());
    }

}
