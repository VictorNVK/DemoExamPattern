package ru.demoexam.backend.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.demoexam.backend.config.OpenApiConfig;
import ru.demoexam.backend.security.SecurityRoles;
import ru.demoexam.backend.service.OrderService;
import ru.demoexam.backend.web.dto.OrderDetailDto;
import ru.demoexam.backend.web.dto.OrderDraftDto;
import ru.demoexam.backend.web.dto.OrderOptionsDto;
import ru.demoexam.backend.web.dto.OrderSummaryDto;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Заказы", description = "Работа с заказами")
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    @PreAuthorize("hasAnyRole('" + SecurityRoles.MANAGER + "', '" + SecurityRoles.ADMIN + "')")
    @Operation(summary = "Список заказов", description = "Менеджер и администратор")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    ResponseEntity<List<OrderSummaryDto>> findAll() {
        return orderService.findAll();
    }

    @GetMapping("/options")
    @PreAuthorize("hasRole('" + SecurityRoles.ADMIN + "')")
    @Operation(summary = "Справочники для редактора заказа", description = "Только администратор")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    ResponseEntity<OrderOptionsDto> options() {
        return orderService.options();
    }

    @GetMapping("/next-id")
    @PreAuthorize("hasRole('" + SecurityRoles.ADMIN + "')")
    @Operation(summary = "Следующий ID заказа", description = "Только администратор")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    ResponseEntity<Integer> nextId() {
        return orderService.nextId();
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('" + SecurityRoles.ADMIN + "')")
    @Operation(summary = "Заказ по ID", description = "Только администратор")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    ResponseEntity<OrderDetailDto> findById(@PathVariable int orderId) {
        return orderService.findById(orderId);
    }

    @PostMapping
    @PreAuthorize("hasRole('" + SecurityRoles.ADMIN + "')")
    @Operation(summary = "Создать заказ", description = "Только администратор")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    ResponseEntity<OrderDetailDto> create(@RequestBody OrderDraftDto draft) {
        return orderService.save(draft);
    }

    @PutMapping("/{orderId}")
    @PreAuthorize("hasRole('" + SecurityRoles.ADMIN + "')")
    @Operation(summary = "Обновить заказ", description = "Только администратор")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    ResponseEntity<OrderDetailDto> update(@PathVariable int orderId, @RequestBody OrderDraftDto draft) {
        return orderService.save(copyDraft(orderId, draft));
    }

    @DeleteMapping("/{orderId}")
    @PreAuthorize("hasRole('" + SecurityRoles.ADMIN + "')")
    @Operation(summary = "Удалить заказ", description = "Только администратор")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    ResponseEntity<Void> delete(@PathVariable int orderId) {
        return orderService.delete(orderId);
    }

    private OrderDraftDto copyDraft(int orderId, OrderDraftDto draft) {
        return OrderDraftDto.builder()
                .id(orderId)
                .customerName(draft.customerName())
                .managerId(draft.managerId())
                .status(draft.status())
                .pickupAddress(draft.pickupAddress())
                .orderDate(draft.orderDate())
                .deliveryDate(draft.deliveryDate())
                .pickupCode(draft.pickupCode())
                .comment(draft.comment())
                .productId(draft.productId())
                .quantity(draft.quantity())
                .unitPrice(draft.unitPrice())
                .discountPercent(draft.discountPercent())
                .build();
    }
}
