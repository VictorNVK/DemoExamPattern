package ru.demoexam.backend.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.demoexam.backend.config.OpenApiConfig;
import ru.demoexam.backend.security.SecurityRoles;
import ru.demoexam.backend.service.ProductService;
import ru.demoexam.backend.web.dto.ProductDraftDto;
import ru.demoexam.backend.web.dto.ProductFilterOptionsDto;
import ru.demoexam.backend.web.dto.ProductListItemDto;
import ru.demoexam.backend.web.dto.ProductOptionsDto;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Товары", description = "Каталог товаров")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @PreAuthorize("permitAll()")
    @Operation(
            summary = "Список товаров",
            description = "Гость, клиент, менеджер, администратор. "
                    + "Поиск, сортировка и фильтр по скидке — для менеджера и администратора (на клиенте)."
    )
    @SecurityRequirements
    ResponseEntity<List<ProductListItemDto>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "ALL") String supplierFilter,
            @RequestParam(required = false, defaultValue = "ALL") String discountFilter,
            @RequestParam(required = false, defaultValue = "NONE") String sortField,
            @RequestParam(required = false, defaultValue = "ASC") String sortDirection
    ) {
        return productService.findAll(search, supplierFilter, discountFilter, sortField, sortDirection);
    }

    @GetMapping("/filter-options")
    @PreAuthorize("hasAnyRole('" + SecurityRoles.MANAGER + "', '" + SecurityRoles.ADMIN + "')")
    @Operation(summary = "Справочники для фильтра каталога", description = "Менеджер и администратор")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    ResponseEntity<ProductFilterOptionsDto> filterOptions() {
        return productService.filterOptions();
    }

    @GetMapping("/options")
    @PreAuthorize("hasRole('" + SecurityRoles.ADMIN + "')")
    @Operation(summary = "Справочники для редактора", description = "Только администратор")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    ResponseEntity<ProductOptionsDto> options() {
        return productService.options();
    }

    @GetMapping("/next-id")
    @PreAuthorize("hasRole('" + SecurityRoles.ADMIN + "')")
    @Operation(summary = "Следующий ID товара", description = "Только администратор")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    ResponseEntity<Integer> nextId() {
        return productService.nextId();
    }

    @GetMapping("/{productId}")
    @PreAuthorize("hasRole('" + SecurityRoles.ADMIN + "')")
    @Operation(summary = "Товар по ID", description = "Только администратор (форма редактирования)")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    ResponseEntity<ProductDraftDto> findById(@PathVariable int productId) {
        return productService.findById(productId);
    }

    @PostMapping
    @PreAuthorize("hasRole('" + SecurityRoles.ADMIN + "')")
    @Operation(summary = "Создать товар", description = "Только администратор")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    ResponseEntity<ProductDraftDto> create(@RequestBody ProductDraftDto draft) {
        return productService.save(draft);
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('" + SecurityRoles.ADMIN + "')")
    @Operation(summary = "Обновить товар", description = "Только администратор")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    ResponseEntity<ProductDraftDto> update(@PathVariable int productId, @RequestBody ProductDraftDto draft) {
        return productService.update(productId, draft);
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('" + SecurityRoles.ADMIN + "')")
    @Operation(summary = "Удалить товар", description = "Только администратор")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    ResponseEntity<Void> delete(@PathVariable int productId) throws Exception {
        return productService.delete(productId);
    }
}
