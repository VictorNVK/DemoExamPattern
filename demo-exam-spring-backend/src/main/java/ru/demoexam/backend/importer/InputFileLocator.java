package ru.demoexam.backend.importer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.demoexam.backend.config.AppProperties;

@Component
@RequiredArgsConstructor
public class InputFileLocator {

    private final AppProperties appProperties;

    public List<Path> candidateDirectories() {
        Set<Path> candidates = new LinkedHashSet<>();
        Path workingDirectory = Path.of("").toAbsolutePath().normalize();

        addIfPresent(candidates, Path.of(appProperties.inputRoot()));
        addIfPresent(candidates, workingDirectory.resolve(appProperties.inputRoot()));
        addIfPresent(candidates, workingDirectory.resolve("input"));
        addIfPresent(candidates, workingDirectory.resolve("../test_files"));

        Path parent = workingDirectory.getParent();
        if (parent != null) {
            addIfPresent(candidates, parent.resolve("test_files"));
        }

        if (candidates.isEmpty()) {
            candidates.add(workingDirectory.resolve("input"));
        }
        return new ArrayList<>(candidates);
    }

    public Path primaryInputDirectory() {
        for (Path candidate : candidateDirectories()) {
            if (Files.isDirectory(candidate)) {
                return candidate.toAbsolutePath().normalize();
            }
        }
        return Path.of(appProperties.inputRoot()).toAbsolutePath().normalize();
    }

    public Optional<Path> findFile(String exactName, String namePart) {
        for (Path directory : candidateDirectories()) {
            if (!Files.isDirectory(directory)) {
                continue;
            }
            try {
                Optional<Path> found = findInDirectory(directory, exactName, namePart);
                if (found.isPresent()) {
                    return found;
                }
            } catch (IOException exception) {
                // skip unreadable directory
            }
        }
        return Optional.empty();
    }

    public Optional<Path> findAsset(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return Optional.empty();
        }
        String trimmedName = fileName.trim();
        for (Path directory : candidateDirectories()) {
            if (!Files.isDirectory(directory)) {
                continue;
            }
            Path asset = directory.resolve(trimmedName);
            if (Files.isRegularFile(asset)) {
                return Optional.of(asset);
            }
        }
        return Optional.empty();
    }

    public List<String> describeSearchLocations() {
        List<String> locations = new ArrayList<>();
        for (Path directory : candidateDirectories()) {
            locations.add(directory.toAbsolutePath().normalize().toString());
        }
        return locations;
    }

    private Optional<Path> findInDirectory(Path directory, String exactName, String namePart) throws IOException {
        Path exactPath = directory.resolve(exactName);
        if (Files.isRegularFile(exactPath)) {
            return Optional.of(exactPath);
        }

        if (namePart == null || namePart.isBlank()) {
            return Optional.empty();
        }

        String normalizedPart = namePart.toLowerCase(Locale.ROOT);
        try (var stream = Files.list(directory)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        String fileName = path.getFileName().toString().toLowerCase(Locale.ROOT);
                        return fileName.contains(normalizedPart) && fileName.endsWith(".xlsx");
                    })
                    .sorted()
                    .findFirst();
        }
    }

    private void addIfPresent(Set<Path> candidates, Path path) {
        if (path != null) {
            candidates.add(path.toAbsolutePath().normalize());
        }
    }
}
