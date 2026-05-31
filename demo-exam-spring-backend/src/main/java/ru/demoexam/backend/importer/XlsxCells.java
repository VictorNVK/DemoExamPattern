package ru.demoexam.backend.importer;

import java.util.Locale;
import java.util.Map;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;

final class XlsxCells {

    private static final DataFormatter DATA_FORMATTER = new DataFormatter(Locale.ROOT);

    private XlsxCells() {
    }

    static String text(Row row, int columnIndex) {
        if (row == null || columnIndex < 0) {
            return "";
        }
        if (row.getCell(columnIndex) == null) {
            return "";
        }
        return DATA_FORMATTER.formatCellValue(row.getCell(columnIndex)).trim();
    }

    static String normalizeHeader(String value) {
        return value
                .toLowerCase(Locale.ROOT)
                .replace('ё', 'е')
                .replaceAll("[^a-z0-9а-я]", "");
    }

    static String mapRole(String roleRaw, Map<String, String> roleMap) {
        if (roleRaw == null || roleRaw.isBlank()) {
            return "client";
        }
        return roleMap.getOrDefault(roleRaw.trim(), "client");
    }
}
