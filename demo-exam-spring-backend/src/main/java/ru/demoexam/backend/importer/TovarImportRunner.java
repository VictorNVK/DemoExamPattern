package ru.demoexam.backend.importer;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TovarImportRunner implements ApplicationRunner {

    private final ExamDataImportService examDataImportService;

    @Override
    public void run(ApplicationArguments args) {
        examDataImportService.importIfDatabaseEmpty();
    }
}
