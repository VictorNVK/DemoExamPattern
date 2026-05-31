package ru.demoexam.backend.importer;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;

@Component
public class TovarXlsxParser {

    private static final DataFormatter DATA_FORMATTER = new DataFormatter(Locale.ROOT);

    private enum Column {
        ARTICLE,
        NAME,
        UNIT,
        PRICE,
        SUPPLIER,
        MANUFACTURER,
        CATEGORY,
        DISCOUNT,
        STOCK,
        DESCRIPTION,
        PHOTO
    }

    public List<TovarRow> parse(Path xlsxFile) throws IOException {
        try (InputStream inputStream = Files.newInputStream(xlsxFile);
                Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
            if (sheet == null) {
                return List.of();
            }

            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            if (headerRow == null) {
                return List.of();
            }

            Map<Column, Integer> columns = resolveColumns(headerRow);
            boolean hasHeader = !columns.isEmpty();
            int firstDataRow = hasHeader ? sheet.getFirstRowNum() + 1 : sheet.getFirstRowNum();

            List<TovarRow> rows = new ArrayList<>();
            for (int rowIndex = firstDataRow; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }

                TovarRow parsedRow = columns.isEmpty()
                        ? parsePositionalRow(row)
                        : parseMappedRow(row, columns);
                if (parsedRow != null) {
                    rows.add(parsedRow);
                }
            }
            return rows;
        }
    }

    private TovarRow parsePositionalRow(Row row) {
        String article = cellText(row, 0);
        if (article.isBlank()) {
            return null;
        }

        return new TovarRow(
                article,
                cellText(row, 1),
                defaultUnit(cellText(row, 2)),
                parseDecimal(cellText(row, 3)),
                cellText(row, 4),
                cellText(row, 5),
                cellText(row, 6),
                parseDecimal(cellText(row, 7)),
                parseInteger(cellText(row, 8)),
                cellText(row, 9),
                cellText(row, 10)
        );
    }

    private TovarRow parseMappedRow(Row row, Map<Column, Integer> columns) {
        String article = cellText(row, columns.get(Column.ARTICLE));
        if (article.isBlank()) {
            return null;
        }

        return new TovarRow(
                article,
                cellText(row, columns.get(Column.NAME)),
                defaultUnit(cellText(row, columns.get(Column.UNIT))),
                parseDecimal(cellText(row, columns.get(Column.PRICE))),
                cellText(row, columns.get(Column.SUPPLIER)),
                cellText(row, columns.get(Column.MANUFACTURER)),
                cellText(row, columns.get(Column.CATEGORY)),
                parseDecimal(cellText(row, columns.get(Column.DISCOUNT))),
                parseInteger(cellText(row, columns.get(Column.STOCK))),
                cellText(row, columns.get(Column.DESCRIPTION)),
                cellText(row, columns.get(Column.PHOTO))
        );
    }

    private Map<Column, Integer> resolveColumns(Row headerRow) {
        Map<Column, Integer> columns = new HashMap<>();
        for (Cell cell : headerRow) {
            Column column = mapHeader(cellText(headerRow, cell.getColumnIndex()));
            if (column != null) {
                columns.put(column, cell.getColumnIndex());
            }
        }

        if (!columns.containsKey(Column.ARTICLE)) {
            return Map.of();
        }
        return columns;
    }

    private Column mapHeader(String header) {
        String normalized = normalizeHeader(header);
        if (normalized.isBlank()) {
            return null;
        }

        if (matches(normalized, "артикул", "article", "id")) {
            return Column.ARTICLE;
        }
        if (matches(normalized, "наименованиетовара", "наименование", "name", "название")) {
            return Column.NAME;
        }
        if (matches(normalized, "единицизмерения", "едизм", "unit")) {
            return Column.UNIT;
        }
        if (matches(normalized, "цена", "price", "стоимость")) {
            return Column.PRICE;
        }
        if (matches(normalized, "поставщик", "supplier")) {
            return Column.SUPPLIER;
        }
        if (matches(normalized, "производитель", "manufacturer", "изготовитель")) {
            return Column.MANUFACTURER;
        }
        if (matches(normalized, "категориятовара", "категория", "category")) {
            return Column.CATEGORY;
        }
        if (matches(normalized, "действующаяскидка", "скидка", "discount")) {
            return Column.DISCOUNT;
        }
        if (matches(normalized, "количествоноскладе", "количество", "остаток", "stock")) {
            return Column.STOCK;
        }
        if (matches(normalized, "описаниетовара", "описание", "description")) {
            return Column.DESCRIPTION;
        }
        if (matches(normalized, "фото", "photo", "изображение", "картинка")) {
            return Column.PHOTO;
        }
        return null;
    }

    private boolean matches(String normalized, String... variants) {
        for (String variant : variants) {
            if (normalized.equals(variant) || normalized.contains(variant)) {
                return true;
            }
        }
        return false;
    }

    private String normalizeHeader(String value) {
        return value
                .toLowerCase(Locale.ROOT)
                .replace('ё', 'е')
                .replaceAll("[^a-z0-9а-я]", "");
    }

    private String cellText(Row row, Integer columnIndex) {
        if (columnIndex == null || columnIndex < 0) {
            return "";
        }
        return cellText(row, columnIndex.intValue());
    }

    private String cellText(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            return "";
        }
        return DATA_FORMATTER.formatCellValue(cell).trim();
    }

    private String defaultUnit(String unit) {
        if (unit.isBlank()) {
            return "шт.";
        }
        return unit;
    }

    private BigDecimal parseDecimal(String rawValue) {
        String normalized = rawValue.trim().replace('%', ' ').replace(',', '.').trim();
        if (normalized.isBlank()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(normalized);
        } catch (NumberFormatException exception) {
            return BigDecimal.ZERO;
        }
    }

    private int parseInteger(String rawValue) {
        String normalized = rawValue.trim().replace(',', '.');
        if (normalized.isBlank()) {
            return 0;
        }
        try {
            return (int) Math.round(Double.parseDouble(normalized));
        } catch (NumberFormatException exception) {
            return 0;
        }
    }
}
