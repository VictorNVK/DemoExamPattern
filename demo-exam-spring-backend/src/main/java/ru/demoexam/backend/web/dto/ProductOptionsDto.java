package ru.demoexam.backend.web.dto;

import java.util.List;
import lombok.Builder;

@Builder
public record ProductOptionsDto(
        List<String> categories,
        List<String> manufacturers,
        List<String> suppliers,
        List<String> units
) {
}
