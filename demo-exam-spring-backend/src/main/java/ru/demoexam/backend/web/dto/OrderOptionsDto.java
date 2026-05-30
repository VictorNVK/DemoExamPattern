package ru.demoexam.backend.web.dto;

import java.util.List;
import lombok.Builder;

@Builder
public record OrderOptionsDto(
        List<LookupItemDto> managers,
        List<LookupItemDto> products,
        List<String> statuses,
        List<String> pickupAddresses
) {
}
