package ru.demoexam.backend.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.demoexam.backend.domain.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    Optional<Product> findFirstByOrderByIdDesc();

    default int findMaxId() {
        return findFirstByOrderByIdDesc().map(Product::getId).orElse(1000);
    }
}
