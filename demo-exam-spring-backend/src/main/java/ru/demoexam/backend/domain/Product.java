package ru.demoexam.backend.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    private Integer id;

    private String article;
    private String name;
    private String category;
    private String description;
    private String manufacturer;
    private String supplier;
    private String unit;
    private BigDecimal price;
    private Integer stockQuantity;
    private BigDecimal discountPercent;
    private String imagePath;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
