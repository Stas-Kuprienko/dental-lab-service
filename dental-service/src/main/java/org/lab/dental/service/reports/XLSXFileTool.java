package org.lab.dental.service.reports;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.lab.exception.ApplicationCustomException;
import org.lab.exception.BadRequestCustomException;
import org.springframework.stereotype.Component;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Component
public class XLSXFileTool {

    public static final String PATIENT = "ПАЦИЕНТ";
    public static final String CLINIC = "КЛИНИКА";


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
            ByteArrayInputStream inputStream = new ByteArrayInputStream(out.toByteArray());
            log.info("XLSX file report is wrote successfully");
            return inputStream;
        } catch (IOException e) {
            log.error("Error while writing XLSX file report", e);
            throw new ApplicationCustomException("Failed to write XLSX file report", e);
        }
    }

    public List<List<String>> parseReport(ByteArrayInputStream inputStream, List<String> titles) {
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            Iterator<Sheet> sheetIterator = workbook.sheetIterator();
            if (sheetIterator.hasNext()) {
                Sheet sheet = sheetIterator.next();
                List<String> sheetHeader = new ArrayList<>();
                Iterator<Row> rowIterator = sheet.iterator();
                List<List<String>> sheetData = new ArrayList<>();
                sheetData.add(sheetHeader);
                if (validateHeader(rowIterator, titles, sheetHeader)) {
                    while (rowIterator.hasNext()) {
                        Row row = rowIterator.next();
                        if (row.getPhysicalNumberOfCells() == 0) {
                            continue;
                        }
                        List<String> rowData = new ArrayList<>();
                        int i = 0;
                        // set patient
                        rowData.add(row.getCell(i++).getStringCellValue());
                        // set clinic
                        rowData.add(row.getCell(i++).getStringCellValue());
                        // set product quantities
                        while (i < sheetHeader.size()) {
                            Cell cell = row.getCell(i++);
                            String cellValue = getCellValue(cell);
                            rowData.add(cellValue);
                        }
                        sheetData.add(rowData);
                    }
                }
                return sheetData;
            } else {
                throw new BadRequestCustomException("Provided empty sheet file");
            }
        } catch (IOException e) {
            throw new ApplicationCustomException("Failed to read Excel file", e);
        }
    }


    private String getCellValue(Cell cell) {
        String cellValue = "";
        if (cell != null) {
            cellValue = switch (cell.getCellType()) {
                case NUMERIC -> String.valueOf(cell.getNumericCellValue());
                case STRING -> cell.getStringCellValue();
                case BLANK -> "";
                default -> throw new BadRequestCustomException("Unexpected report cell value: " + cell.getCellType());
            };
        }
        return cellValue;
    }

    private boolean validateHeader(Iterator<Row> rowIterator, List<String> titles, List<String> headerSheet) {
        if (!rowIterator.hasNext()) {
            throw new BadRequestCustomException("Provided empty sheet file");
        }
        Row header = rowIterator.next();
        int i = 0;
        String value = getCellStringValue(header, i);
        if (!validate(value, PATIENT)) {
            throw new BadRequestCustomException(errorMessage + rowIterator);
        }
        headerSheet.add(PATIENT);
        i++;
        value = getCellStringValue(header, i);
        if (!validate(value, CLINIC)) {
            throw new BadRequestCustomException(errorMessage + rowIterator);
        }
        headerSheet.add(CLINIC);
        i++;
        List<String> titlesCopy = new ArrayList<>(List.copyOf(titles));
        // iterate header cell and validate product titles
        for (;i < header.getPhysicalNumberOfCells(); i++) {
            value = getCellStringValue(header, i);
            boolean result = false;
            for (int n = 0; n < titlesCopy.size(); n++) {
                String title = titlesCopy.get(n);
                // if product title cell value validated, remove this from copy of title list
                if (validate(value, title)) {
                    result = true;
                    headerSheet.add(title);
                    titlesCopy.remove(n);
                    break;
                }
            }
            if (!result) {
                throw new BadRequestCustomException("Unrecognized ProductType title: " + value);
            }
            // in result validated header data list with correct product titles is gotten
        }
        return true;
    }

    private String getCellStringValue(Row row, int i) {
        return row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
    }

    private boolean validate(String value, String expected) {
        if (value == null) {
            return false;
        }
        value = value.replaceAll("[^\\p{L}\\p{Nd}]", "");
        return value.equalsIgnoreCase(expected);
    }

    private static final String errorMessage = """
                    Provided invalid sheet file. Required template:
                    ______________________________________________________________________
                    ПАЦИЕНТ | КЛИНИКА | ${ProductType.title} | ${ProductType.title} | ....
                    ----------------------------------------------------------------------
                    Accepted file:
                    """;
}
