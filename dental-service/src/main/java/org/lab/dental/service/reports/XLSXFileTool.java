package org.lab.dental.service.reports;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.lab.exception.ApplicationCustomException;
import org.lab.model.ProductType;
import org.springframework.stereotype.Component;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Component
public class XLSXFileTool {


    public ByteArrayInputStream createReport(List<List<String>> sheetData) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("Report");
            for (int i = 0; i < sheetData.size(); i++) {
                Row row = sheet.createRow(i);
                List<String> rowData = sheetData.get(i);
                for (int j = 0; j < rowData.size(); j++) {
                    Cell cell = row.createCell(j);
                    cell.setCellValue(rowData.get(j));
                }
            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            log.error("Error while creating Excel report", e);
            throw new ApplicationCustomException("Failed to generate Excel file", e);
        }
    }

    public void readReport(ByteArrayInputStream inputStream, List<ProductType> productTypes) {
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            Iterator<Sheet> sheetIterator = workbook.sheetIterator();
            while (sheetIterator.hasNext()) {
                Sheet sheet = sheetIterator.next();

                for (Row row : sheet) {
                    //TODO validate patient and clinic then iterate and validate products

                }
            }
        } catch (IOException e) {
            log.error("Error while reading Excel report", e);
            throw new ApplicationCustomException("Failed to read Excel file", e);
        }
    }


    private boolean validateSheet(Sheet sheet, List<ProductType> productTypes) {
        Row row = sheet.iterator().next();
        byte i = 0;
        String value = getCellStringValue(row, i);
        if (!validate(value, ReportMapper.PATIENT)) {
            return false;
        }
        i++;
        value = getCellStringValue(row, i);
        if (!validate(value, ReportMapper.CLINIC)) {
            return false;
        }
        i++;
        for (;i < row.getPhysicalNumberOfCells(); i++) {
            value = getCellStringValue(row, i);
            boolean result = false;
            for (ProductType pt : productTypes) {
                if (validate(value, pt.getTitle())) {
                    result = true;
                    break;
                }
            }
            if (!result) {
                return false;
            }
        }
        return true;
    }

    private String getCellStringValue(Row row, byte i) {
        return row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
    }

    private boolean validate(String value, String expected) {
        return value != null && value.equalsIgnoreCase(expected);
    }
}
