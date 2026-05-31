package ru.demoexam.backend.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.demoexam.backend.domain.Order;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    long countByProduct_Id(Integer productId);

    Optional<Order> findFirstByOrderByIdDesc();

    default int findMaxId() {
        return findFirstByOrderByIdDesc().map(Order::getId).orElse(1000);
    }

    @EntityGraph(attributePaths = {"manager", "product"})
    List<Order> findAllByOrderByOrderDateDescIdDesc();

    default List<Order> findAllForList() {
        return findAllByOrderByOrderDateDescIdDesc();
    }
}
