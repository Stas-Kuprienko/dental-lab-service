package org.lab.dental.service.reports;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.lab.exception.ApplicationCustomException;
import org.springframework.stereotype.Component;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
}
