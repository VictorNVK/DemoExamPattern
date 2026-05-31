package ru.demoexam.backend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.demoexam.backend.domain.Order;
import ru.demoexam.backend.domain.Product;
import ru.demoexam.backend.domain.User;
import ru.demoexam.backend.repository.OrderRepository;
import ru.demoexam.backend.repository.ProductRepository;
import ru.demoexam.backend.repository.UserRepository;
import ru.demoexam.backend.web.dto.LookupItemDto;
import ru.demoexam.backend.web.dto.OrderDetailDto;
import ru.demoexam.backend.web.dto.OrderDraftDto;
import ru.demoexam.backend.web.dto.OrderOptionsDto;
import ru.demoexam.backend.web.dto.OrderSummaryDto;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final List<String> DEFAULT_STATUSES = List.of(
            "Новый",
            "В обработке",
            "Завершён",
            "Отменён"
    );

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public ResponseEntity<List<OrderSummaryDto>> findAll() {
        List<OrderSummaryDto> result = new ArrayList<>();
        for (Order order : orderRepository.findAllForList()) {
            result.add(toSummary(order));
        }
        return ResponseEntity.ok(result);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<OrderDetailDto> findById(int orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Заказ не найден."));
        return ResponseEntity.ok(toDetail(order));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Integer> nextId() {
        return ResponseEntity.ok(orderRepository.findMaxId() + 1);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<OrderOptionsDto> options() {
        List<LookupItemDto> managers = new ArrayList<>();
        for (User user : userRepository.findByRoleInAndActiveTrueOrderByFullNameAsc(
                List.of("manager", "admin")
        )) {
            managers.add(LookupItemDto.builder()
                    .id(user.getId())
                    .label(user.getFullName())
                    .build());
        }

        List<LookupItemDto> products = new ArrayList<>();
        for (Product product : productRepository.findAll()) {
            String article = product.getArticle();
            if (article == null || article.isBlank()) {
                article = "P" + product.getId();
            }
            products.add(LookupItemDto.builder()
                    .id(product.getId())
                    .label(article + " — " + product.getName())
                    .build());
        }

        TreeSet<String> pickupAddresses = new TreeSet<>();
        for (Order order : orderRepository.findAll()) {
            String pickupAddress = order.getPickupAddress();
            if (pickupAddress != null && !pickupAddress.trim().isEmpty()) {
                pickupAddresses.add(pickupAddress.trim());
            }
        }

        return ResponseEntity.ok(OrderOptionsDto.builder()
                .managers(managers)
                .products(products)
                .statuses(DEFAULT_STATUSES)
                .pickupAddresses(new ArrayList<>(pickupAddresses))
                .build());
    }

    @Transactional
    public ResponseEntity<OrderDetailDto> save(OrderDraftDto draft) {
        validateDraft(draft);

        Order existing = orderRepository.findById(draft.id()).orElse(null);
        User manager = userRepository.findById(draft.managerId())
                .orElseThrow(() -> new IllegalArgumentException("Менеджер не найден."));
        Product product = productRepository.findById(draft.productId())
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден."));

        Order order = Order.builder()
                .id(draft.id())
                .customerName(draft.customerName().trim())
                .manager(manager)
                .status(draft.status().trim())
                .pickupAddress(draft.pickupAddress().trim())
                .orderDate(draft.orderDate())
                .deliveryDate(draft.deliveryDate())
                .pickupCode(draft.pickupCode() == null ? "" : draft.pickupCode().trim())
                .comment(draft.comment() == null ? "" : draft.comment().trim())
                .product(product)
                .quantity(draft.quantity())
                .unitPrice(draft.unitPrice())
                .discountPercent(draft.discountPercent() == null ? BigDecimal.ZERO : draft.discountPercent())
                .build();

        orderRepository.save(order);

        HttpStatus status = existing == null ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(toDetail(order));
    }

    @Transactional
    public ResponseEntity<Void> delete(int orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Заказ не найден."));
        orderRepository.delete(order);
        return ResponseEntity.noContent().build();
    }

    private void validateDraft(OrderDraftDto draft) {
        if (draft.customerName() == null || draft.customerName().isBlank()) {
            throw new IllegalArgumentException("Укажите ФИО клиента.");
        }
        if (draft.managerId() == null) {
            throw new IllegalArgumentException("Выберите менеджера.");
        }
        if (draft.status() == null || draft.status().isBlank()) {
            throw new IllegalArgumentException("Укажите статус заказа.");
        }
        if (draft.pickupAddress() == null || draft.pickupAddress().isBlank()) {
            throw new IllegalArgumentException("Укажите адрес пункта выдачи.");
        }
        if (draft.orderDate() == null) {
            throw new IllegalArgumentException("Укажите дату заказа.");
        }
        if (draft.productId() == null) {
            throw new IllegalArgumentException("Выберите товар.");
        }
        if (draft.quantity() == null || draft.quantity() <= 0) {
            throw new IllegalArgumentException("Количество должно быть больше нуля.");
        }
        if (draft.unitPrice() == null || draft.unitPrice().signum() < 0) {
            throw new IllegalArgumentException("Укажите корректную цену.");
        }
    }

    private OrderSummaryDto toSummary(Order order) {
        return OrderSummaryDto.builder()
                .id(order.getId())
                .customerName(blankToDefault(order.getCustomerName(), "Без клиента"))
                .managerName(order.getManager() == null ? "Не назначен" : order.getManager().getFullName())
                .statusName(blankToDefault(order.getStatus(), "Не указан"))
                .orderDate(order.getOrderDate())
                .deliveryDate(order.getDeliveryDate())
                .pickupAddress(blankToDefault(order.getPickupAddress(), ""))
                .pickupCode(blankToDefault(order.getPickupCode(), ""))
                .productArticle(resolveArticle(order.getProduct()))
                .productName(order.getProduct() == null ? "" : order.getProduct().getName())
                .comment(blankToDefault(order.getComment(), ""))
                .itemsCount(order.getQuantity() == null ? 0 : order.getQuantity())
                .totalAmount(calculateTotal(order))
                .build();
    }

    private OrderDetailDto toDetail(Order order) {
        return OrderDetailDto.builder()
                .id(order.getId())
                .customerName(blankToDefault(order.getCustomerName(), ""))
                .managerId(order.getManager() == null ? 0 : order.getManager().getId())
                .managerName(order.getManager() == null ? "" : order.getManager().getFullName())
                .status(blankToDefault(order.getStatus(), ""))
                .pickupAddress(blankToDefault(order.getPickupAddress(), ""))
                .orderDate(order.getOrderDate())
                .deliveryDate(order.getDeliveryDate())
                .pickupCode(blankToDefault(order.getPickupCode(), ""))
                .comment(blankToDefault(order.getComment(), ""))
                .productId(order.getProduct() == null ? 0 : order.getProduct().getId())
                .productArticle(resolveArticle(order.getProduct()))
                .productName(order.getProduct() == null ? "" : order.getProduct().getName())
                .quantity(order.getQuantity() == null ? 0 : order.getQuantity())
                .unitPrice(order.getUnitPrice() == null ? BigDecimal.ZERO : order.getUnitPrice())
                .discountPercent(order.getDiscountPercent() == null ? BigDecimal.ZERO : order.getDiscountPercent())
                .totalAmount(calculateTotal(order))
                .build();
    }

    private BigDecimal calculateTotal(Order order) {
        if (order.getUnitPrice() == null || order.getQuantity() == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal discount = order.getDiscountPercent() == null ? BigDecimal.ZERO : order.getDiscountPercent();
        return order.getUnitPrice()
                .multiply(BigDecimal.valueOf(order.getQuantity()))
                .multiply(BigDecimal.ONE.subtract(discount.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private String resolveArticle(Product product) {
        if (product == null) {
            return "";
        }
        if (product.getArticle() != null && !product.getArticle().isBlank()) {
            return product.getArticle();
        }
        return "P" + product.getId();
    }

    private String blankToDefault(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }
}
