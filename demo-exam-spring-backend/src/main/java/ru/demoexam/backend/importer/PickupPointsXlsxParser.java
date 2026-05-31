package ru.demoexam.backend.importer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;

@Component
public class PickupPointsXlsxParser {

    public List<String> parse(Path xlsxFile) throws IOException {
        try (InputStream inputStream = Files.newInputStream(xlsxFile);
                Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
            if (sheet == null) {
                return List.of();
            }

            List<String> pickupPoints = new ArrayList<>();
            for (int rowIndex = sheet.getFirstRowNum(); rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }
                String address = XlsxCells.text(row, 0);
                if (!address.isBlank()) {
                    pickupPoints.add(address);
                }
            }
            return pickupPoints;
        }
    }
}
