/*
 * Copyright (c) 2018-2020, RTE (https://www.rte-france.com)
 * Copyright (c) 2019-2020 RTE international (https://www.rte-international.com)
 * See AUTHORS.txt
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 * This file is part of the Let’s Coordinate project.
 */

package org.lfenergy.letscoordinate.backend.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.lfenergy.letscoordinate.backend.config.CoordinationConfig;
import org.lfenergy.letscoordinate.backend.config.LetscoProperties;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.EventMessageDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.payload.*;
import org.lfenergy.letscoordinate.backend.dto.reporting.RscKpiDto;
import org.lfenergy.letscoordinate.backend.dto.reporting.RscKpiReportDataDto;
import org.lfenergy.letscoordinate.backend.dto.reporting.RscKpiReportSubmittedFormDataDto;
import org.lfenergy.letscoordinate.backend.enums.*;
import org.lfenergy.letscoordinate.backend.exception.InvalidInputFileException;
import org.lfenergy.letscoordinate.backend.service.EventMessageService;
import org.lfenergy.letscoordinate.backend.util.Constants;
import org.lfenergy.letscoordinate.backend.util.DateUtil;
import org.lfenergy.letscoordinate.backend.util.PojoUtil;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExcelDataProcessor implements DataProcessor {

    private final LetscoProperties letscoProperties;
    private final CoordinationConfig coordinationConfig;
    private final EventMessageService eventMessageService;


    private final String XMLNS = "http://iec.ch/TC57/2011/schema/message";
    private final int EFFECTIVE_DATA_SHEET_INDEX = 0;
    private final List<String> EMPTY_CELL_SYMBOLS = Arrays.asList("", "-");

    private final List<String> headerExclusiveColNames = Arrays.asList("verb", "noun", "timestamp", "source", "messageId");
    private final List<String> headerPropertiesExclusiveColNames = Arrays.asList("format");
    private final List<String> headerBusinessDateIdentifierColNames = Arrays.asList("messageType", "messageTypeName", "businessDayFrom",
            "businessDayTo", "processStep", "timeframe", "timeframeNumber", "sendingUser", "fileName", "tso", "biddingZone");
    private final List<String> headerPropertiesColNames = Stream.of(headerPropertiesExclusiveColNames, headerBusinessDateIdentifierColNames)
            .flatMap(Collection::stream).collect(Collectors.toList());

    private final List<String> headerColNames = Stream.of(headerExclusiveColNames, headerPropertiesColNames)
            .flatMap(Collection::stream).collect(Collectors.toList());

    private final String dataTypeColName = "dataType";
    private final String nameColName = "name";
    private final String labelColName = "label";
    private final String timestampColName = "timestamp";
    private final String eicCodeColName = "eicCode";
    private final String valueColName = "value";
    private final List<String> payloadTextDataColNames = Arrays.asList(nameColName, valueColName);
    private final List<String> payloadLinkDataColNames = Arrays.asList(nameColName, valueColName, eicCodeColName);
    private final List<String> payloadDataColNames = Arrays.asList("id", labelColName, timestampColName, eicCodeColName, valueColName,
            "accept", "reject", "explanation", "comment");

    private final List<String> payloadColNames = Stream.of(Arrays.asList(dataTypeColName, nameColName), payloadDataColNames)
            .flatMap(Collection::stream).collect(Collectors.toList());


    public EventMessageDto inputStreamToPojo(String filePath, InputStream inputStream)
            throws IOException, NoSuchFieldException, IllegalAccessException, InstantiationException, InvalidInputFileException {
        Workbook workbook = new XSSFWorkbook(inputStream);

        EventMessageDto eventMessageDto = new EventMessageDto();
        eventMessageDto.setXmlns(XMLNS);

        Map<ExcelBlocEnum, Map<String, Integer>> columnIndexMap = validateExcelFileAndMapColTitlesIndexesByBloc(workbook);

        Sheet sheet = workbook.getSheetAt(EFFECTIVE_DATA_SHEET_INDEX);
        initEventMessageHeader(eventMessageDto, sheet, columnIndexMap.get(ExcelBlocEnum.HEADER));
        initEventMessagePayload(eventMessageDto, sheet, columnIndexMap.get(ExcelBlocEnum.PAYLOAD));

        eventMessageService.checkEicCodes(eventMessageDto);

        log.info("POJO Generated from Excel file \"{}\" => {}", filePath, eventMessageDto);

        return eventMessageDto;
    }

    /**
     * Allows to validate the format of the input {@link Workbook} and to generate a map that has as keys
     * the excel block names (e.g HEADER, PAYLOAD) and as values the map of each column's title and its index
     *
     * @param workbook the input {@link Workbook} to validate
     * @return the colTitleIndexMapByBloc if the {@link Workbook} is valid, throws {@link InvalidInputFileException}
     * exception otherwise
     */
    private Map<ExcelBlocEnum, Map<String, Integer>> validateExcelFileAndMapColTitlesIndexesByBloc(Workbook workbook)
            throws InvalidInputFileException{
        Map<ExcelBlocEnum, Map<String, Integer>> colTitleIndexMapByBloc = new LinkedHashMap<>();
        if (workbook == null)
            return colTitleIndexMapByBloc;

        Sheet effectiveDataSheet = workbook.getSheetAt(EFFECTIVE_DATA_SHEET_INDEX);
        if (effectiveDataSheet != null) {
            colTitleIndexMapByBloc.put(ExcelBlocEnum.HEADER, getColTitlesIndexesForExcelBloc(ExcelBlocEnum.HEADER, headerColNames, effectiveDataSheet));
            colTitleIndexMapByBloc.put(ExcelBlocEnum.PAYLOAD, getColTitlesIndexesForExcelBloc(ExcelBlocEnum.PAYLOAD, payloadColNames, effectiveDataSheet));
        } else {
            throw new InvalidInputFileException("effective data sheet not found!");
        }

        log.debug("colTitleIndexMapByBloc: {}", colTitleIndexMapByBloc);
        return colTitleIndexMapByBloc;
    }

    /**
     * Allows to generate for a given excel block, a map that has as key the column title and as value the
     * corresponding column index.
     *
     * @param excelBloc the excel block (e.g HEADER, PAYLOAD)
     * @param colTitleNames the colTitle names to map with their indexes
     * @param sheet the {@link Workbook}'s sheet to process
     * @return The colTitleIndexMap if the {@link Workbook} is valid, throws {@link InvalidInputFileException}
     * exception otherwise
     */
    private Map<String, Integer> getColTitlesIndexesForExcelBloc(ExcelBlocEnum excelBloc,
                                                                 List<String> colTitleNames,
                                                                 Sheet sheet) throws InvalidInputFileException{
        Map<String, Integer> colTitleIndexMap = new LinkedHashMap<>();
        boolean acceptPropertiesIgnoreCase = letscoProperties.getInputFile().getValidation().isAcceptPropertiesIgnoreCase();
        boolean failOnUnknownProperties = letscoProperties.getInputFile().getValidation().isFailOnUnknownProperties();
        List<String> adaptedColTitleNames = acceptPropertiesIgnoreCase
                ? colTitleNames.stream().map(String::toLowerCase).collect(Collectors.toList())
                : colTitleNames;

        Row titlesRow = sheet.getRow(excelBloc.getTitlesRowIndex());
        if (titlesRow != null) {
            for (int i = titlesRow.getFirstCellNum(); i <= titlesRow.getLastCellNum(); i++) {
                Cell cell = titlesRow.getCell(i);
                if (cell != null && StringUtils.isNotBlank(cell.getStringCellValue())) {
                    boolean colTitleNameFound = adaptedColTitleNames.contains(acceptPropertiesIgnoreCase ? cell.getStringCellValue().toLowerCase() : cell.getStringCellValue());
                    if (failOnUnknownProperties && !colTitleNameFound)
                        throw new InvalidInputFileException("column \"" + cell.getStringCellValue() + "\" is not allowed in the " + excelBloc.name() + " block!");
                    Integer index = colTitleIndexMap.get(cell.getStringCellValue());
                    if (index != null)
                        throw new InvalidInputFileException("column \"" + cell.getStringCellValue() + "\" found more than once in the " + excelBloc.name() + " block!");
                    if(colTitleNameFound) {
                        // Allways save the key on lowerCase
                        colTitleIndexMap.put(cell.getStringCellValue().toLowerCase(), cell.getColumnIndex());
                    }
                }
            }
        } else {
            throw new InvalidInputFileException("title row not found for the " + excelBloc.name() + " block!");
        }
        return colTitleIndexMap;
    }

    /**
     * Allows to create the header block of the eventMessage object
     *
     * @param eventMessageDto the object to update
     * @param sheet the data {@link Sheet}
     * @param columnIndexMap map to link the column title with its index
     * @throws NoSuchFieldException see {@link PojoUtil#setProperty(Object, String, String)}
     * @throws IllegalAccessException see {@link PojoUtil#setProperty(Object, String, String)}
     * @see ExcelDataProcessor#setProperties(Object, List, Row, Map)
     */
    public void initEventMessageHeader(EventMessageDto eventMessageDto,
                                              Sheet sheet,
                                              Map<String, Integer> columnIndexMap) throws NoSuchFieldException, IllegalAccessException {
        if (sheet != null) {
            if (eventMessageDto == null)
                eventMessageDto = new EventMessageDto();

            Row currentRow = sheet.getRow(ExcelBlocEnum.HEADER.getTitlesRowIndex()+1);

            setProperties(eventMessageDto.getHeader(), headerExclusiveColNames, currentRow, columnIndexMap);
            setProperties(eventMessageDto.getHeader().getProperties(), headerPropertiesExclusiveColNames, currentRow, columnIndexMap);
            setProperties(eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier(), headerBusinessDateIdentifierColNames, currentRow, columnIndexMap);
        }
    }

    /**
     * Allows to create the payload block of the eventMessage object
     *
     * @param eventMessageDto the object to update
     * @param sheet the data {@link Sheet}
     * @param columnIndexMap map to link the column title with its index
     * @throws NoSuchFieldException see {@link PojoUtil#setProperty(Object, String, String)}
     * @throws IllegalAccessException see {@link PojoUtil#setProperty(Object, String, String)}
     * @throws InstantiationException see {@link #generatePayloadComplexeDataType(Map, Class, Class, Class)}
     * @see ExcelDataProcessor#getPayloadDataDetails(Map, DataTypeEnum, Class)
     */
    public void initEventMessagePayload(EventMessageDto eventMessageDto,
                                               Sheet sheet,
                                               Map<String, Integer> columnIndexMap)
            throws NoSuchFieldException, InstantiationException, IllegalAccessException {
        if (eventMessageDto != null && sheet != null && columnIndexMap != null) {
            Map<DataTypeEnum, Map<String, List<IPayloadData>>> payloadMap = buildPayloadAsMap(sheet, columnIndexMap);

            log.debug("PayloadMap: ");
            payloadMap.forEach((key, value) -> log.debug("  |-- {} : {}", key, value));

            for (DataTypeEnum dataTypeKey : payloadMap.keySet()) {
                switch (dataTypeKey) {
                    case TEXT:
                        eventMessageDto.getPayload().setText(getPayloadDataDetails(payloadMap, dataTypeKey, TextDataDto.class));
                        break;
                    case LINK:
                        eventMessageDto.getPayload().setLinks(getPayloadDataDetails(payloadMap, dataTypeKey, LinkDataDto.class));
                        break;
                    case RSC_KPI:
                        eventMessageDto.getPayload().setRscKpi(getPayloadDataDetails(payloadMap, dataTypeKey, RscKpiDataDto.class));
                        break;
                    case TIMESERIE:
                        eventMessageDto.getPayload().setTimeserie(getPayloadDataDetails(payloadMap, dataTypeKey, TimeserieDataDto.class));
                        break;
                }
            }
        }
    }

    /**
     * Allows to transform the {@link Sheet}'s data to a map having as keys the dataType (e.g TEXT, LINK, RSC_KPI, TIMESERIE)
     * and as values an other map grouping the payload data by dataName (e.g GP1 for RSC_KPI dataType, probabilisticResults
     * for RSC_KPI TIMESERIE)
     *
     * @param sheet the data {@link Sheet}
     * @param columnIndexMap map to link the column title with its index
     * @return
     * @throws NoSuchFieldException see {@link PojoUtil#setProperty(Object, String, String)}
     * @throws IllegalAccessException see {@link PojoUtil#setProperty(Object, String, String)}
     */
    private Map<DataTypeEnum, Map<String, List<IPayloadData>>> buildPayloadAsMap(Sheet sheet,
                                                                                 Map<String, Integer> columnIndexMap) throws NoSuchFieldException, IllegalAccessException, InstantiationException {
        Map<DataTypeEnum, Map<String,List<IPayloadData>>> payloadMap = new LinkedHashMap<>();

        Integer dataTypeColIndex = columnIndexMap.get(dataTypeColName.toLowerCase());
        Integer nameColIndex = columnIndexMap.get(nameColName.toLowerCase());

        if (sheet.getPhysicalNumberOfRows() > 0) {
            for (int i = ExcelBlocEnum.PAYLOAD.getTitlesRowIndex()+1; i <= sheet.getLastRowNum(); i++) {
                if (sheet.getRow(i) != null) {
                    Row currentRow = sheet.getRow(i);
                    String dataTypeCellValue = currentRow.getCell(dataTypeColIndex).getStringCellValue();
                    DataTypeEnum dataType = DataTypeEnum.getByValue(dataTypeCellValue);
                    Map<String,List<IPayloadData>> payloadDataMap = payloadMap.get(dataType);
                    if (payloadDataMap == null) {
                        payloadDataMap = new LinkedHashMap<>();
                        payloadMap.put(dataType, payloadDataMap);
                    }
                    String nameCellValue = currentRow.getCell(nameColIndex).getStringCellValue();
                    List nameDataList = payloadDataMap.get(nameCellValue);
                    if (nameDataList == null) {
                        nameDataList = new ArrayList();
                        payloadDataMap.put(nameCellValue, nameDataList);
                    }
                    switch (dataType) {
                        case TEXT:
                            TextDataDto textDataDto = new TextDataDto();
                            setProperties(textDataDto, payloadTextDataColNames, currentRow, columnIndexMap);
                            nameDataList.add(textDataDto);
                            break;
                        case LINK:
                            LinkDataDto linkDataDto = new LinkDataDto();
                            setProperties(linkDataDto, payloadLinkDataColNames, currentRow, columnIndexMap);
                            nameDataList.add(linkDataDto);
                            break;
                        case RSC_KPI:
                            createRscKpiDataAndAddToDataList(columnIndexMap, currentRow, nameDataList);
                            break;
                        case TIMESERIE:
                            createTimeserieDataAndAddToDataList(columnIndexMap, currentRow, nameDataList);
                            break;
                    }

                }
            }
        }
        return payloadMap;
    }

    private void createRscKpiDataAndAddToDataList(Map<String, Integer> columnIndexMap,
                                                         Row currentRow,
                                                         List nameDataList) throws NoSuchFieldException, IllegalAccessException {
        OffsetDateTime offsetDateTime = DateUtil.toOffsetDateTime(getCellValueAsString(currentRow.getCell(columnIndexMap.get(timestampColName))));
        String label = getCellValueAsString(currentRow.getCell(columnIndexMap.get(labelColName)));
        RscKpiDataDetailsDto dataDetailsDto = null;
        for (Object data : nameDataList) {
            if (data != null && data instanceof RscKpiDataDetailsDto
                    && ((RscKpiDataDetailsDto) data).getTimestamp() != null
                    && ((RscKpiDataDetailsDto) data).getTimestamp().isEqual(offsetDateTime)
                    && ((RscKpiDataDetailsDto) data).getLabel() != null
                    && ((RscKpiDataDetailsDto) data).getLabel().equals(label)) {
                dataDetailsDto = (RscKpiDataDetailsDto) data;
                break;
            }
        }
        if (dataDetailsDto == null) {
            dataDetailsDto = new RscKpiDataDetailsDto();
            dataDetailsDto.setTimestamp(offsetDateTime);
            dataDetailsDto.setLabel(label);
            nameDataList.add(dataDetailsDto);
        }
        RscKpiTemporalDataDto temporalDataDto = new RscKpiTemporalDataDto();
        dataDetailsDto.getDetail().add(temporalDataDto);

        List<String> fieldNames = Stream.of(RscKpiTemporalDataDto.class.getDeclaredFields()).map(Field::getName).collect(Collectors.toList());
        setProperties(temporalDataDto, fieldNames, currentRow, columnIndexMap);
    }

    private void createTimeserieDataAndAddToDataList(Map<String, Integer> columnIndexMap,
                                                            Row currentRow,
                                                            List nameDataList) throws NoSuchFieldException, IllegalAccessException {
        OffsetDateTime offsetDateTime = DateUtil.toOffsetDateTime(getCellValueAsString(currentRow.getCell(columnIndexMap.get(timestampColName))));
        TimeserieDataDetailsDto dataDetailsDto = null;
        for (Object data : nameDataList) {
            if (data != null && data instanceof TimeserieDataDetailsDto
                    && ((TimeserieDataDetailsDto) data).getTimestamp() != null
                    && ((TimeserieDataDetailsDto) data).getTimestamp().isEqual(offsetDateTime)) {
                dataDetailsDto = (TimeserieDataDetailsDto) data;
                break;
            }
        }
        if (dataDetailsDto == null) {
            dataDetailsDto = new TimeserieDataDetailsDto();
            dataDetailsDto.setTimestamp(offsetDateTime);
            nameDataList.add(dataDetailsDto);
        }
        TimeserieTemporalDataDto temporalDataDto = new TimeserieTemporalDataDto();
        dataDetailsDto.getDetail().add(temporalDataDto);

        List<String> fieldNames = Stream.of(TimeserieTemporalDataDto.class.getDeclaredFields()).map(Field::getName).collect(Collectors.toList());
        setProperties(temporalDataDto, fieldNames, currentRow, columnIndexMap);
    }

    /**
     * Allows to transform the payload data of a given dataType from its Map< String,List< IPayloadDataType>> form to a
     * List<T> form (with T is a type that extends IPayloadDataType witch is passed as argument to the method too)
     *
     * @param payloadMap the payload data as map
     * @param dataType the dataType to identify the part of data to transform
     * @param type the type of the objects to return as list
     * @param <T> the generic type of the objects to return as list
     * @return the payload data as a list of objects having the given generic type passed as argument
     */
    private <T extends IPayloadData> List<T> getPayloadDataDetails(Map<DataTypeEnum, Map<String, List<IPayloadData>>> payloadMap,
                                                                          DataTypeEnum dataType,
                                                                          Class<T> type) throws InstantiationException, IllegalAccessException {
        if(payloadMap == null || payloadMap.get(dataType) == null)
            return null;
        List<T> result = new ArrayList<>();
        Map<String, List<IPayloadData>> payloadDetailsMap = payloadMap.get(dataType);
        if(type == TextDataDto.class || type == LinkDataDto.class) {
            for (Object object : payloadDetailsMap.values().stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList())) {
                result.add((T) object);
            }
        } else if (type == RscKpiDataDto.class){
            result.addAll(generatePayloadComplexeDataType(payloadDetailsMap, type, RscKpiDataDto.class, RscKpiDataDetailsDto.class));
        } else {
            result.addAll(generatePayloadComplexeDataType(payloadDetailsMap, type, TimeserieDataDto.class, TimeserieDataDetailsDto.class));
        }
        return result;
    }

    /**
     * Allows to generate the payload for the complex data (extending {@link AbstractPayloadComplexData})
     *
     * @param payloadDetailsMap map grouping the payload data by dataName (e.g GP1 for RSC_KPI dataType,
     *                          probabilisticResults for RSC_KPI TIMESERIE)
     * @param resultType a Class< T exends {@link IPayloadData}> to represent the output type
     * @param dataType a Class< T1 extends AbstractPayloadComplexData> to represent the input data type
     * @param dataDetailsType a Class< T2 extends IPayloadDataDetails> to represent the input data details (subdata) type
     * @return List of {@link AbstractPayloadComplexData} (subtype of {@link IPayloadData})
     * @throws InstantiationException if the instantiation from the generic type T1 is not possible
     * @throws IllegalAccessException if the class or its nullary constructor is not accessible.
     * @see IPayloadData
     */
    private <T extends IPayloadData, T1 extends AbstractPayloadComplexData & IPayloadData, T2 extends IPayloadDataDetails>
    List<T> generatePayloadComplexeDataType(Map<String, List<IPayloadData>> payloadDetailsMap,
                                            Class<T> resultType,
                                            Class<T1> dataType,
                                            Class<T2> dataDetailsType) throws InstantiationException, IllegalAccessException {
        List<T> result = new ArrayList<>();
        for (String key : payloadDetailsMap.keySet()) {
            List<IPayloadData> list = payloadDetailsMap.get(key);
            T1 dataDto = dataType.newInstance();
            dataDto.setName(key);
            dataDto.setData(new ArrayList<>());
            for (Object obj : list) {
                dataDto.getData().add((T2) obj);
            }
            result.add((T) dataDto);
        }
        return result;
    }

    /**
     * Allows for each {@link Cell} of the given {@link Row} to set the value to the appropriate object's attribute
     * according to the colNames list and the columnIndexMap
     *
     * @param object object to update with row's values
     * @param colNames the column names used for looping the row's cells
     * @param row the {@link Row} from witch we get the values
     * @param columnIndexMap the map to link the right value with the attribute target using the column index
     * @throws NoSuchFieldException if no field found matching the column name
     * @throws IllegalAccessException if the found field is not accessible (this exception is resolved internally,
     * so it's never thrown but should be declared)
     */
    private void setProperties(Object object,
                               List<String> colNames,
                               Row row,
                               Map<String, Integer> columnIndexMap) throws NoSuchFieldException, IllegalAccessException {
        for (String colName : colNames) {
            Integer index = columnIndexMap.get(colName.toLowerCase());
            if (index != null) {
                String cellValue = getCellValueAsString(row.getCell(index));
                if (!EMPTY_CELL_SYMBOLS.contains(cellValue))
                    PojoUtil.setProperty(object, colName, cellValue);
            }
        }
    }

    private String getCellValueAsString (Cell cell) {
        if (cell == null) return null;
        CellType cellType = cell.getCellTypeEnum() == CellType.FORMULA
                ? cell.getCachedFormulaResultTypeEnum()
                : cell.getCellTypeEnum();
        switch (cellType) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC: return String.valueOf(((Double)cell.getNumericCellValue()).intValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            default: return "";
        }
    }

    /* * * * * * * * * * * * * * * * * * * *\
    |*   GENERATING RSC KPI EXCEL REPORT   *|
    \* * * * * * * * * * * * * * * * * * * */

    public byte[] generateRscKpiExcelReport(RscKpiReportDataDto rscKpiReportDataDto) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        final CellStyle COLUMN_TITLE_STYLE = workbook.createCellStyle();
        COLUMN_TITLE_STYLE.setVerticalAlignment(VerticalAlignment.CENTER);
        COLUMN_TITLE_STYLE.setAlignment(HorizontalAlignment.CENTER);
        if (rscKpiReportDataDto != null
                && rscKpiReportDataDto.getSubmittedFormData() != null
                && rscKpiReportDataDto.getRscKpiTypedDataMap() != null
                && rscKpiReportDataDto.getRscKpiSubtypedDataMap() != null) {
            // the GP dataType is managed first, then the BPs
            Map aux = rscKpiReportDataDto.getRscKpiTypedDataMap().remove(KpiDataTypeEnum.BP);
            if(aux != null)
                rscKpiReportDataDto.getRscKpiTypedDataMap().put(KpiDataTypeEnum.BP, aux);

            // Create data
            // Map<KpiDataTypeEnum, Map<KpiDataSubtypeEnum, Map<String, List<RscKpiDto.DataDto>>>>
            for(KpiDataTypeEnum kpiDataTypeEnum : rscKpiReportDataDto.getRscKpiTypedDataMap().keySet()) {
                Map<KpiDataSubtypeEnum, Map<String, List<RscKpiDto.DataDto>>> subtypeMap = rscKpiReportDataDto.getRscKpiTypedDataMap().get(kpiDataTypeEnum);
                // Create sheet for the GPx/BPx
                for(KpiDataSubtypeEnum kpiDataSubtypeEnum : subtypeMap.keySet()) {
                    Map<String, List<RscKpiDto.DataDto>> dataList = subtypeMap.get(kpiDataSubtypeEnum);
                    // Create sheet
                    XSSFSheet sheet = workbook.createSheet(kpiDataSubtypeEnum.name());
                    int lastColumnNumber = 0;
                    // Create first row with the GPx/BPx title
                    int rowCount = 0;
                    Row row = sheet.createRow(rowCount++);
                    int columnCount = 0;
                    Cell cell = row.createCell(columnCount++);
                    CoordinationConfig.KpiDataSubtype kpiDataSubtype = rscKpiReportDataDto.getRscKpiSubtypedDataMap().get(kpiDataSubtypeEnum.name());
                    cell.setCellValue("KPI " + kpiDataSubtypeEnum.name() + (kpiDataSubtype != null ? " - " + kpiDataSubtype.getName() : ""));
                    cell.setCellStyle(COLUMN_TITLE_STYLE);

                    if (rscKpiReportDataDto.getSubmittedFormData().getDataGranularity() == DataGranularityEnum.DAILY) { // CASE: DAILY VIEW
                        // Prepare data (letscoEntityList) to be used to do sheetMergeRegion for the first coming row (graph_legend_row)
                        // and to be used as data for the second coming row (letsco_entity_row)
                        List<CoordinationConfig.LetscoEntity> letscoEntityList = new ArrayList<>();
                        if (CollectionUtils.isNotEmpty(rscKpiReportDataDto.getSubmittedFormData().getRscCodes())) {
                            if (isAllRscSelected(rscKpiReportDataDto.getSubmittedFormData()))
                                letscoEntityList.add(0, CoordinationConfig.Rsc.builder()
                                        .eicCode(Constants.ALL_RSCS_CODE)
                                        .name(Constants.ALL_RSCS_NAME)
                                        .shortName(Constants.ALL_RSCS_NAME)
                                        .index(0).build());
                            else
                                letscoEntityList.add(coordinationConfig.getRscByEicCode(rscKpiReportDataDto.getSubmittedFormData().getRscCodes().get(0)));
                        } else {
                            if (isAllRegionSelected(rscKpiReportDataDto.getSubmittedFormData()))
                                letscoEntityList.add(0, CoordinationConfig.Region.builder()
                                        .eicCode(Constants.ALL_REGIONS_CODE)
                                        .name(Constants.ALL_REGIONS_NAME)
                                        .shortName(Constants.ALL_REGIONS_NAME)
                                        .index(0).build());
                            else
                                letscoEntityList.add(coordinationConfig.getRegionByEicCode(rscKpiReportDataDto.getSubmittedFormData().getRegionCodes().get(0)));
                        }

                        if (kpiDataTypeEnum == KpiDataTypeEnum.BP && isAllRscOrRegionSelected(rscKpiReportDataDto.getSubmittedFormData())) {
                            if (isAllRscSelected(rscKpiReportDataDto.getSubmittedFormData())) {
                                letscoEntityList.addAll(coordinationConfig.getRscs().values().stream()
                                        .sorted(Comparator.comparing(CoordinationConfig.Rsc::getIndex))
                                        .collect(Collectors.toList()));
                            } else if (isAllRegionSelected(rscKpiReportDataDto.getSubmittedFormData())) {
                                letscoEntityList.addAll(coordinationConfig.getRegions().values().stream()
                                        .sorted(Comparator.comparing(CoordinationConfig.Region::getIndex))
                                        .collect(Collectors.toList()));
                            }
                        }
                        // Create graph_legend_row
                        int graphLegendRowNumber = rowCount;
                        row = sheet.createRow(rowCount++);
                        columnCount = 0;
                        cell = row.createCell(columnCount++);
                        cell.setCellValue("Date");
                        cell.setCellStyle(COLUMN_TITLE_STYLE);
                        if (letscoEntityList.size() > 1) {
                            sheet.addMergedRegion(new CellRangeAddress(graphLegendRowNumber, graphLegendRowNumber + 1, 0, 0));
                        }
                        List<String> colTitleList = new ArrayList();
                        if (CollectionUtils.isNotEmpty(dataList.keySet())) {
                            int letscoEntityColumnNumber = columnCount;
                            for (String dataName : dataList.keySet()) {
                                colTitleList.add(dataName);
                                cell = row.createCell(columnCount);
                                cell.setCellValue(dataName);
                                cell.setCellStyle(COLUMN_TITLE_STYLE);
                                if (letscoEntityList.size() > 1) {
                                    sheet.addMergedRegion(new CellRangeAddress(graphLegendRowNumber, graphLegendRowNumber, columnCount, columnCount+=(letscoEntityList.size()-1)));
                                }
                                columnCount++;
                            }
                        } else {
                            String dataName = kpiDataSubtype != null ? kpiDataSubtype.getName() : kpiDataSubtypeEnum.name();
                            colTitleList.add(dataName);
                            cell = row.createCell(columnCount);
                            cell.setCellValue(dataName);
                            cell.setCellStyle(COLUMN_TITLE_STYLE);
                            if (letscoEntityList.size() > 1) {
                                sheet.addMergedRegion(new CellRangeAddress(graphLegendRowNumber, graphLegendRowNumber, columnCount, columnCount+=(letscoEntityList.size()-1)));
                            }
                            columnCount++;
                        }
                        lastColumnNumber = columnCount;
                        // Create letsco_entity_row (only for BP dataType)
                        if (kpiDataTypeEnum == KpiDataTypeEnum.BP && isAllRscOrRegionSelected(rscKpiReportDataDto.getSubmittedFormData())) {
                            row = sheet.createRow(rowCount++);
                            columnCount = 0;
                            cell = row.createCell(columnCount++);
                            cell.setCellValue("Date");
                            cell.setCellStyle(COLUMN_TITLE_STYLE);
                            for (String colTitle : colTitleList) {
                                for (CoordinationConfig.LetscoEntity letscoEntity : letscoEntityList) {
                                    cell = row.createCell(columnCount++);
                                    cell.setCellValue(letscoEntity.getName());
                                    cell.setCellStyle(COLUMN_TITLE_STYLE);
                                }
                            }
                        }

                        // Create data rows
                        LocalDate startDate = rscKpiReportDataDto.getSubmittedFormData().getStartDate();
                        LocalDate incrementedDate = LocalDate.of(startDate.getYear(), startDate.getMonth(), startDate.getDayOfMonth());
                        LocalDate endDate = rscKpiReportDataDto.getSubmittedFormData().getEndDate();
                        while (incrementedDate.isBefore(endDate) || incrementedDate.isEqual(endDate)) {
                            row = sheet.createRow(rowCount++);
                            // First column is for dates
                            columnCount = 0;
                            cell = row.createCell(columnCount++);
                            cell.setCellValue(DateTimeFormatter.ISO_LOCAL_DATE.format(incrementedDate));
                            // data columns
                            for (String colTitle : colTitleList) {
                                for (CoordinationConfig.LetscoEntity letscoEntity : letscoEntityList) {
                                    boolean letscoEntityValueFound = false;
                                    cell = row.createCell(columnCount++);
                                    cell.setCellValue(0);
                                    List<RscKpiDto.DataDto> dataDtoList = dataList.get(colTitle);
                                    if (dataDtoList != null) {
                                        dataDtoList = dataDtoList.stream().
                                                sorted(Comparator.comparing(RscKpiDto.DataDto::getTimestamp).reversed())
                                                .collect(Collectors.toList());
                                        for (RscKpiDto.DataDto datumDto : dataDtoList) {
                                            if (datumDto != null && CollectionUtils.isNotEmpty(datumDto.getDetails())
                                                    && incrementedDate.isEqual(datumDto.getTimestamp())) {
                                                for (RscKpiDto.DataDto.DetailsDto detailsDto : datumDto.getDetails()) {
                                                    if (letscoEntity.getEicCode().equals(detailsDto.getEicCode())) {
                                                        cell.setCellValue(detailsDto.getValue());
                                                        letscoEntityValueFound = true;
                                                        break;
                                                    }
                                                }
                                                if (letscoEntityValueFound) {
                                                    break;
                                                } else if (kpiDataTypeEnum == KpiDataTypeEnum.GP) {
                                                    for (RscKpiDto.DataDto.DetailsDto detailsDto : datumDto.getDetails()) {
                                                        if (detailsDto.getEicCode() == null) {
                                                            cell.setCellValue(detailsDto.getValue());
                                                            letscoEntityValueFound = true;
                                                            break;
                                                        }
                                                    }
                                                }
                                                if (letscoEntityValueFound)
                                                    break;
                                            }
                                        }
                                    }
                                }
                            }
                            // increment date
                            incrementedDate = incrementedDate.plusDays(1);
                        }
                    } else { // CASE: MULTI-YEAR VIEW
                        // List of selected Entities (RSC or Region)
                        List<CoordinationConfig.LetscoEntity> selectedLetscoEntityList = getSelectedLetscoEntityList(rscKpiReportDataDto.getSubmittedFormData());
                        // Create second row for columns titles
                        row = sheet.createRow(rowCount++);
                        columnCount = 0;
                        cell = row.createCell(columnCount++);
                        cell.setCellValue("Date");
                        cell.setCellStyle(COLUMN_TITLE_STYLE);
                        for (CoordinationConfig.LetscoEntity selectedEntity : selectedLetscoEntityList) {
                            cell = row.createCell(columnCount++);
                            cell.setCellValue(selectedEntity.getName());
                            cell.setCellStyle(COLUMN_TITLE_STYLE);
                        }
                        lastColumnNumber = columnCount;
                        // Create data rows
                        int startYear = rscKpiReportDataDto.getSubmittedFormData().getStartDate().getYear();
                        int endYear = rscKpiReportDataDto.getSubmittedFormData().getEndDate().getYear();
                        int incrementedYear = startYear;
                        while (incrementedYear <= endYear) {
                            row = sheet.createRow(rowCount++);
                            // First column is for years
                            columnCount = 0;
                            cell = row.createCell(columnCount++);
                            cell.setCellValue(incrementedYear);
                            // data columns
                            for (CoordinationConfig.LetscoEntity selectedEntity : selectedLetscoEntityList) {
                                cell = row.createCell(columnCount++);
                                cell.setCellValue(getDataDetailsByEicCode(selectedEntity.getEicCode(), kpiDataTypeEnum, dataList.get(""+incrementedYear))
                                        .map(RscKpiDto.DataDto.DetailsDto::getValue)
                                        .orElse(0l));
                            }
                            // increment year
                            incrementedYear++;
                        }
                    }
                    // add Merge region for the sheet title cell and autosize sheet columns
                    sheet.addMergedRegion(new CellRangeAddress(0, 0 , 0, lastColumnNumber - 1));
                    for (int i = 0   ; i < lastColumnNumber ; i++)
                        sheet.autoSizeColumn(i, true);
                }
            }
        }

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            workbook.write(byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        }
    }

    /**
     * check if Pan-EU item checked for RSCs or Regions
     *
     * @param submittedFormDataDto
     * @return true if Pan-EU item checked for RSCs or Regions, else false
     */
    private boolean isAllRscOrRegionSelected(RscKpiReportSubmittedFormDataDto submittedFormDataDto) {
        return isAllRscSelected(submittedFormDataDto) || isAllRegionSelected(submittedFormDataDto);
    }

    /**
     * check if Pan-EU item checked for RSCs
     *
     * @param submittedFormDataDto
     * @return true if Pan-EU item checked for RSCs, else false
     */
    private boolean isAllRscSelected(RscKpiReportSubmittedFormDataDto submittedFormDataDto) {
        return submittedFormDataDto != null
                && CollectionUtils.isNotEmpty(submittedFormDataDto.getRscCodes())
                && submittedFormDataDto.getRscCodes().contains(Constants.ALL_RSCS_CODE);
    }

    /**
     * check if Pan-EU item checked for Regions
     *
     * @param submittedFormDataDto
     * @return true if Pan-EU item checked for Regions, else false
     */
    private boolean isAllRegionSelected(RscKpiReportSubmittedFormDataDto submittedFormDataDto) {
        return submittedFormDataDto != null
                && CollectionUtils.isNotEmpty(submittedFormDataDto.getRegionCodes())
                && submittedFormDataDto.getRegionCodes().contains(Constants.ALL_REGIONS_CODE);
    }

    private List<CoordinationConfig.LetscoEntity> getSelectedLetscoEntityList(RscKpiReportSubmittedFormDataDto submittedFormData) {
        List<CoordinationConfig.LetscoEntity> letscoEntityList = new ArrayList<>();
        if (submittedFormData != null) {
            if (CollectionUtils.isNotEmpty(submittedFormData.getRscCodes())) {
                letscoEntityList = coordinationConfig.getRscs().values().stream()
                        .filter(r -> submittedFormData.getRscCodes().contains(r.getEicCode()))
                        .sorted(Comparator.comparing(CoordinationConfig.Rsc::getIndex))
                        .collect(Collectors.toList());
                if (submittedFormData.getRscCodes().contains(Constants.ALL_RSCS_CODE))
                    letscoEntityList.add(0, CoordinationConfig.Rsc.builder()
                            .eicCode(Constants.ALL_RSCS_CODE)
                            .name(Constants.ALL_RSCS_NAME)
                            .shortName(Constants.ALL_RSCS_NAME)
                            .index(0).build());
            } else if (CollectionUtils.isNotEmpty(submittedFormData.getRegionCodes())) {
                letscoEntityList = coordinationConfig.getRegions().values().stream()
                        .filter(r -> submittedFormData.getRegionCodes().contains(r.getEicCode()))
                        .sorted(Comparator.comparing(CoordinationConfig.Region::getIndex))
                        .collect(Collectors.toList());
                if (submittedFormData.getRegionCodes().contains(Constants.ALL_REGIONS_CODE))
                    letscoEntityList.add(0, CoordinationConfig.Region.builder()
                            .eicCode(Constants.ALL_REGIONS_CODE)
                            .name(Constants.ALL_REGIONS_NAME)
                            .shortName(Constants.ALL_REGIONS_NAME)
                            .index(0).build());
            }
        }
        return letscoEntityList;
    }

    private Optional<RscKpiDto.DataDto.DetailsDto> getDataDetailsByEicCode(String eicCode,
                                                                           KpiDataTypeEnum kpiDataTypeEnum,
                                                                           List<RscKpiDto.DataDto> dataDtoList) {
        if(eicCode == null || kpiDataTypeEnum == null || CollectionUtils.isEmpty(dataDtoList))
            return Optional.empty();
        // sort desc by timestamp to get latest value
        dataDtoList = dataDtoList.stream()
                .sorted(Comparator.comparing(RscKpiDto.DataDto::getTimestamp).reversed())
                .collect(Collectors.toList());
        // get value (dataDetails) by eicCode
        for (RscKpiDto.DataDto data : dataDtoList) {
            for (RscKpiDto.DataDto.DetailsDto details: data.getDetails()) {
                if (details != null && eicCode.equals(details.getEicCode())) {
                    return Optional.of(details);
                }
            }
        }
        // for GP case and value for specific eicCode not found, we should return global value if found (eicCode = null)
        if (kpiDataTypeEnum == KpiDataTypeEnum.GP) {
            for (RscKpiDto.DataDto data : dataDtoList) {
                for (RscKpiDto.DataDto.DetailsDto details: data.getDetails()) {
                    if (details != null && details.getEicCode() == null) {
                        return Optional.of(details);
                    }
                }
            }
        }
        // if nothing found
        return Optional.empty();
    }
}
