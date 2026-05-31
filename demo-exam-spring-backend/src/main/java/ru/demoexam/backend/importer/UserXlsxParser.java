package ru.demoexam.backend.importer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;

@Component
public class UserXlsxParser {

    private static final Map<String, String> ROLE_MAP = Map.of(
            "Администратор", "admin",
            "Менеджер", "manager",
            "Авторизованный клиент", "client"
    );

    public List<UserImportRow> parse(Path xlsxFile) throws IOException {
        try (InputStream inputStream = Files.newInputStream(xlsxFile);
                Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
            if (sheet == null) {
                return List.of();
            }

            List<UserImportRow> rows = new ArrayList<>();
            for (int rowIndex = sheet.getFirstRowNum() + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }

                String roleRaw = XlsxCells.text(row, 0);
                String fullName = XlsxCells.text(row, 1);
                String login = XlsxCells.text(row, 2);
                String password = XlsxCells.text(row, 3);
                if (login.isBlank() || password.isBlank()) {
                    continue;
                }

                rows.add(new UserImportRow(
                        fullName,
                        login,
                        password,
                        XlsxCells.mapRole(roleRaw, ROLE_MAP)
                ));
            }
            return rows;
        }
    }
}
