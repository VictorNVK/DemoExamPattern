package ru.demoexam.backend.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.demoexam.backend.domain.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    Optional<Product> findFirstByOrderByIdDesc();

    default int findMaxId() {
        return findFirstByOrderByIdDesc().map(Product::getId).orElse(1000);
    }

    @Query("""
            SELECT DISTINCT p.category
            FROM Product p
            WHERE p.category IS NOT NULL AND TRIM(p.category) <> ''
            ORDER BY p.category
            """)
    List<String> findDistinctCategories();

    @Query("""
            SELECT DISTINCT p.manufacturer
            FROM Product p
            WHERE p.manufacturer IS NOT NULL AND TRIM(p.manufacturer) <> ''
            ORDER BY p.manufacturer
            """)
    List<String> findDistinctManufacturers();

    @Query("""
            SELECT DISTINCT p.supplier
            FROM Product p
            WHERE p.supplier IS NOT NULL AND TRIM(p.supplier) <> ''
            ORDER BY p.supplier
            """)
    List<String> findDistinctSuppliers();

    @Query("""
            SELECT DISTINCT p.unit
            FROM Product p
            WHERE p.unit IS NOT NULL AND TRIM(p.unit) <> ''
            ORDER BY p.unit
            """)
    List<String> findDistinctUnits();
}
