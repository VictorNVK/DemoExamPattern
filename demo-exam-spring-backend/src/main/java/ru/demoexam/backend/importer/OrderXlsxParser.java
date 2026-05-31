package ru.demoexam.backend.importer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;

@Component
public class OrderXlsxParser {

    public List<OrderImportRow> parse(Path xlsxFile, List<String> pickupPoints) throws IOException {
        try (InputStream inputStream = Files.newInputStream(xlsxFile);
                Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
            if (sheet == null) {
                return List.of();
            }

            List<OrderImportRow> rows = new ArrayList<>();
            for (int rowIndex = sheet.getFirstRowNum() + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || XlsxCells.text(row, 0).isBlank()) {
                    continue;
                }

                String articlesRaw = XlsxCells.text(row, 1);
                LocalDateTime orderDate = parseDateTime(row, 2);
                LocalDateTime deliveryDate = parseDateTime(row, 3);
                Object pickupIndex = readPickupIndex(row, 4);
                String customerName = XlsxCells.text(row, 5);
                String pickupCode = XlsxCells.text(row, 6);
                String status = XlsxCells.text(row, 7);
                String pickupAddress = resolvePickupAddress(pickupIndex, pickupPoints);

                for (OrderItem item : parseOrderItems(articlesRaw)) {
                    rows.add(new OrderImportRow(
                            blankToDefault(customerName, "Без клиента"),
                            blankToDefault(status, "Новый"),
                            pickupAddress,
                            orderDate,
                            deliveryDate,
                            pickupCode,
                            item.article(),
                            item.quantity()
                    ));
                }
            }
            return rows;
        }
    }

    private Object readPickupIndex(Row row, int columnIndex) {
        if (row.getCell(columnIndex) == null) {
            return null;
        }
        String text = XlsxCells.text(row, columnIndex);
        if (text.isBlank()) {
            return null;
        }
        try {
            return (int) Math.round(Double.parseDouble(text.replace(',', '.')));
        } catch (NumberFormatException exception) {
            return text;
        }
    }

    private String resolvePickupAddress(Object pickupIndex, List<String> pickupPoints) {
        if (!(pickupIndex instanceof Integer index)) {
            return "";
        }
        if (index >= 1 && index <= pickupPoints.size()) {
            return pickupPoints.get(index - 1);
        }
        return "";
    }

    private List<OrderItem> parseOrderItems(String rawValue) {
        List<OrderItem> items = new ArrayList<>();
        if (rawValue == null || rawValue.isBlank()) {
            return items;
        }

        String[] parts = rawValue.split(",");
        List<String> tokens = new ArrayList<>();
        for (String part : parts) {
            String token = part.trim();
            if (!token.isEmpty()) {
                tokens.add(token);
            }
        }

        int index = 0;
        while (index + 1 < tokens.size()) {
            String article = tokens.get(index);
            int quantity = parseInteger(tokens.get(index + 1));
            items.add(new OrderItem(article, quantity));
            index += 2;
        }
        return items;
    }

    private int parseInteger(String rawValue) {
        try {
            return (int) Math.round(Double.parseDouble(rawValue.replace(',', '.')));
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private LocalDateTime parseDateTime(Row row, int columnIndex) {
        if (row.getCell(columnIndex) == null) {
            return null;
        }

        try {
            return row.getCell(columnIndex).getLocalDateTimeCellValue();
        } catch (Exception ignored) {
            // fallback to text parsing
        }

        String text = XlsxCells.text(row, columnIndex);
        if (text.isBlank()) {
            return null;
        }

        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"),
                DateTimeFormatter.ofPattern("dd.MM.yyyy")
        );

        for (DateTimeFormatter formatter : formatters) {
            try {
                String pattern = formatter.toString();
                if (pattern.contains("HH") || pattern.contains("H")) {
                    return LocalDateTime.parse(text, formatter);
                }
                return LocalDate.parse(text, formatter).atStartOfDay();
            } catch (Exception ignored) {
                // try next pattern
            }
        }
        return LocalDateTime.now();
    }

    private String blankToDefault(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }

    private record OrderItem(String article, int quantity) {
    }
}
