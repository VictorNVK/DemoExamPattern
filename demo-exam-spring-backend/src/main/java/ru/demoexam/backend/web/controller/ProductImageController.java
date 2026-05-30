package ru.demoexam.backend.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.demoexam.backend.config.OpenApiConfig;
import ru.demoexam.backend.security.SecurityRoles;
import ru.demoexam.backend.service.ProductService;
import ru.demoexam.backend.web.dto.ProductDraftDto;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Изображения товаров", description = "Загрузка фото товара")
public class ProductImageController {

    private final ProductService productService;

    @PostMapping("/{productId}/image")
    @PreAuthorize("hasRole('" + SecurityRoles.ADMIN + "')")
    @Operation(summary = "Загрузить изображение", description = "Только администратор")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    ResponseEntity<ProductDraftDto> uploadImage(
            @PathVariable int productId,
            @RequestPart("file") MultipartFile file
    ) throws Exception {
        return productService.uploadImage(productId, file);
    }
}
