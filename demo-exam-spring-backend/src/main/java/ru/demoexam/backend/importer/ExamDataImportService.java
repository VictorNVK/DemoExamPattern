package ru.demoexam.backend.importer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.demoexam.backend.domain.Order;
import ru.demoexam.backend.domain.Product;
import ru.demoexam.backend.domain.User;
import ru.demoexam.backend.repository.OrderRepository;
import ru.demoexam.backend.repository.ProductRepository;
import ru.demoexam.backend.repository.UserRepository;
import ru.demoexam.backend.storage.StoragePaths;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamDataImportService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final StoragePaths storagePaths;
    private final InputFileLocator inputFileLocator;
    private final UserXlsxParser userXlsxParser;
    private final TovarXlsxParser tovarXlsxParser;
    private final PickupPointsXlsxParser pickupPointsXlsxParser;
    private final OrderXlsxParser orderXlsxParser;

    @Transactional
    public void importIfDatabaseEmpty() {
        storagePaths.ensureCreated();

        if (!isDatabaseEmpty()) {
            log.info(
                    "Импорт Excel пропущен: в базе уже есть users={}, products={}, orders={}.",
                    userRepository.count(),
                    productRepository.count(),
                    orderRepository.count()
            );
            return;
        }

        Optional<Path> usersFile = inputFileLocator.findFile("user_import.xlsx", "user_import");
        Optional<Path> productsFile = inputFileLocator.findFile("Tovar.xlsx", "tovar");
        Optional<Path> ordersFile = inputFileLocator.findFile("Заказ_import.xlsx", "заказ");
        Optional<Path> pickupFile = inputFileLocator.findFile("Пункты выдачи_import.xlsx", "выдачи");

        if (productsFile.isEmpty()) {
            log.warn("Файл Tovar.xlsx не найден. Искали в: {}", inputFileLocator.describeSearchLocations());
            log.warn("Положите файлы экзамена в demo-exam-spring-backend/input/ и перезапустите backend.");
            return;
        }

        int importedUsers = importUsers(usersFile);
        int importedProducts = importProducts(productsFile.get());
        int importedOrders = importOrders(ordersFile, pickupFile, importedProducts > 0);

        log.info(
                "Импорт Excel завершён: users={}, products={}, orders={}.",
                importedUsers,
                importedProducts,
                importedOrders
        );
    }

    private boolean isDatabaseEmpty() {
        return userRepository.count() == 0
                && productRepository.count() == 0
                && orderRepository.count() == 0;
    }

    private int importUsers(Optional<Path> usersFile) {
        if (usersFile.isEmpty()) {
            log.warn("user_import.xlsx не найден — пользователи не импортированы.");
            return 0;
        }

        try {
            List<UserImportRow> rows = userXlsxParser.parse(usersFile.get());
            List<User> users = new ArrayList<>();
            for (UserImportRow row : rows) {
                users.add(User.builder()
                        .fullName(row.fullName())
                        .login(row.login())
                        .password(row.password())
                        .role(row.role())
                        .active(true)
                        .build());
            }
            userRepository.saveAll(users);
            log.info("Импортировано {} пользователей из {}.", users.size(), usersFile.get().getFileName());
            return users.size();
        } catch (IOException exception) {
            log.error("Ошибка чтения {}: {}", usersFile.get(), exception.getMessage());
            return 0;
        }
    }

    private int importProducts(Path productsFile) {
        try {
            List<TovarRow> rows = tovarXlsxParser.parse(productsFile);
            if (rows.isEmpty()) {
                log.warn("В {} нет строк для импорта.", productsFile.getFileName());
                return 0;
            }

            LocalDateTime now = LocalDateTime.now();
            List<Product> products = new ArrayList<>();
            int productId = 1;
            for (TovarRow row : rows) {
                String imagePath = copyProductImage(row.photoFileName());
                products.add(Product.builder()
                        .id(productId++)
                        .article(row.article())
                        .name(row.name())
                        .category(row.category())
                        .description(row.description())
                        .manufacturer(row.manufacturer())
                        .supplier(row.supplier())
                        .unit(row.unit())
                        .price(row.price())
                        .stockQuantity(row.stockQuantity())
                        .discountPercent(row.discountPercent())
                        .imagePath(imagePath)
                        .createdAt(now)
                        .updatedAt(now)
                        .build());
            }
            productRepository.saveAll(products);
            log.info("Импортировано {} товаров из {}.", products.size(), productsFile.getFileName());
            return products.size();
        } catch (IOException exception) {
            log.error("Ошибка чтения {}: {}", productsFile, exception.getMessage());
            return 0;
        }
    }

    private int importOrders(Optional<Path> ordersFile, Optional<Path> pickupFile, boolean productsImported) {
        if (ordersFile.isEmpty()) {
            log.warn("Заказ_import.xlsx не найден — заказы не импортированы.");
            return 0;
        }
        if (!productsImported) {
            log.warn("Заказы не импортированы: нет товаров в базе.");
            return 0;
        }

        User manager = findManager();
        if (manager == null) {
            log.warn("Заказы не импортированы: в базе нет менеджера.");
            return 0;
        }

        Map<String, Product> productsByArticle = new HashMap<>();
        for (Product product : productRepository.findAll()) {
            productsByArticle.put(product.getArticle(), product);
        }

        List<String> pickupPoints = List.of();
        if (pickupFile.isPresent()) {
            try {
                pickupPoints = pickupPointsXlsxParser.parse(pickupFile.get());
            } catch (IOException exception) {
                log.warn("Не удалось прочитать {}: {}", pickupFile.get(), exception.getMessage());
            }
        }

        try {
            List<OrderImportRow> rows = orderXlsxParser.parse(ordersFile.get(), pickupPoints);
            List<Order> orders = new ArrayList<>();
            int orderId = 1;
            for (OrderImportRow row : rows) {
                Product product = productsByArticle.get(row.article());
                if (product == null) {
                    continue;
                }

                LocalDateTime orderDate = row.orderDate() == null ? LocalDateTime.now() : row.orderDate();
                orders.add(Order.builder()
                        .id(orderId++)
                        .customerName(row.customerName())
                        .manager(manager)
                        .status(row.status())
                        .pickupAddress(row.pickupAddress())
                        .orderDate(orderDate)
                        .deliveryDate(row.deliveryDate())
                        .pickupCode(row.pickupCode())
                        .comment("")
                        .product(product)
                        .quantity(row.quantity())
                        .unitPrice(product.getPrice())
                        .discountPercent(product.getDiscountPercent())
                        .build());
            }
            orderRepository.saveAll(orders);
            log.info("Импортировано {} заказов из {}.", orders.size(), ordersFile.get().getFileName());
            return orders.size();
        } catch (IOException exception) {
            log.error("Ошибка чтения {}: {}", ordersFile.get(), exception.getMessage());
            return 0;
        }
    }

    private User findManager() {
        for (User user : userRepository.findAll()) {
            if ("manager".equals(user.getRole())) {
                return user;
            }
        }
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            return null;
        }
        return users.get(0);
    }

    private String copyProductImage(String photoFileName) {
        Optional<Path> source = inputFileLocator.findAsset(photoFileName);
        if (source.isEmpty()) {
            return null;
        }

        try {
            storagePaths.ensureCreated();
            Path target = storagePaths.imagesDirectory().resolve(source.get().getFileName());
            Files.copy(source.get(), target, StandardCopyOption.REPLACE_EXISTING);
            return target.getFileName().toString();
        } catch (IOException exception) {
            log.warn("Не удалось скопировать изображение {}: {}", source.get(), exception.getMessage());
            return null;
        }
    }
}
