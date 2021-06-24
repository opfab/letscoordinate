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

package org.lfenergy.letscoordinate.backend.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.xssf.usermodel.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class ExcelUtil {

    public static void shiftRowCells(XSSFRow row, int startIndex, int shiftCount) {
        if (row == null || startIndex - shiftCount < 0) {
            log.error("Unable to shift {} cell(s) from starting index {}", shiftCount, startIndex);
            return;
        }

        for (int i = startIndex ; i < row.getPhysicalNumberOfCells() ; i++) {
            XSSFCell oldCell = row.getCell(i);
            if (oldCell != null) {
                XSSFCell newCell = row.getCell(i-shiftCount);
                if (newCell != null)
                    row.removeCell(newCell);
                newCell = row.createCell(i - shiftCount, oldCell.getCellTypeEnum());
                newCell.setCellStyle(oldCell.getCellStyle());
                ExcelUtil.cloneCellValue(oldCell, newCell);
                row.removeCell(oldCell);
            }
        }
    }

    public static void cloneCellValue(XSSFCell oldCell, XSSFCell newCell) {
        switch (oldCell.getCellTypeEnum()) {
            case STRING:
                newCell.setCellValue(oldCell.getStringCellValue());
                break;
            case NUMERIC:
                newCell.setCellValue(oldCell.getNumericCellValue());
                break;
            case BOOLEAN:
                newCell.setCellValue(oldCell.getBooleanCellValue());
                break;
            case FORMULA:
                newCell.setCellFormula(oldCell.getCellFormula());
                break;
            case ERROR:
                newCell.setCellErrorValue(oldCell.getErrorCellValue());
            case BLANK:
            case _NONE:
            default:
                break;
        }
    }

    public static void createStyledUnusedCell(XSSFWorkbook workbook, XSSFRow row, int index) {
        XSSFCell oldCell = row.getCell(index);
        if (oldCell != null)
            row.removeCell(oldCell);
        oldCell = row.createCell(index, CellType.STRING);

        XSSFColor color = new XSSFColor(new java.awt.Color(191,191,191));
        XSSFCellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setFillForegroundColor(color);
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cellStyle.setAlignment(HorizontalAlignment.RIGHT);

        oldCell.setCellStyle(cellStyle);
    }

}
