package ru.demoexam.backend.web.dto;

import java.util.List;
import lombok.Builder;

@Builder
public record ProductFilterOptionsDto(
        List<String> suppliers
) {
}
