package ru.demoexam.backend.web.dto;

import lombok.Builder;

@Builder
public record LookupItemDto(
        int id,
        String label
) {
}
