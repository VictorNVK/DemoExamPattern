package ru.demoexam.backend.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.demoexam.backend.domain.Product;
import ru.demoexam.backend.repository.OrderRepository;
import ru.demoexam.backend.repository.ProductRepository;
import ru.demoexam.backend.web.dto.ProductDraftDto;
import ru.demoexam.backend.web.dto.ProductFilterOptionsDto;
import ru.demoexam.backend.web.dto.ProductListItemDto;
import ru.demoexam.backend.web.dto.ProductOptionsDto;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ImageStorageService imageStorageService;

    @Transactional(readOnly = true)
    public ResponseEntity<List<ProductListItemDto>> findAll(
            String searchText,
            String supplierFilter,
            String discountFilter,
            String sortField,
            String sortDirection
    ) {
        List<ProductListItemDto> products = new ArrayList<>();
        for (Product product : productRepository.findAll()) {
            products.add(toListItem(product));
        }

        products = filterBySearch(products, searchText);
        products = filterBySupplier(products, supplierFilter);
        products = filterByDiscount(products, discountFilter);
        return ResponseEntity.ok(sortProducts(products, sortField, sortDirection));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<ProductFilterOptionsDto> filterOptions() {
        TreeSet<String> suppliers = new TreeSet<>();
        for (Product product : productRepository.findAll()) {
            addDistinctValue(suppliers, product.getSupplier());
        }
        return ResponseEntity.ok(ProductFilterOptionsDto.builder()
                .suppliers(new ArrayList<>(suppliers))
                .build());
    }

    @Transactional(readOnly = true)
    public ResponseEntity<ProductDraftDto> findById(int productId) {
        return ResponseEntity.ok(loadDraft(productId));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<ProductOptionsDto> options() {
        TreeSet<String> categories = new TreeSet<>();
        TreeSet<String> manufacturers = new TreeSet<>();
        TreeSet<String> suppliers = new TreeSet<>();
        TreeSet<String> units = new TreeSet<>();
        for (Product product : productRepository.findAll()) {
            addDistinctValue(categories, product.getCategory());
            addDistinctValue(manufacturers, product.getManufacturer());
            addDistinctValue(suppliers, product.getSupplier());
            addDistinctValue(units, product.getUnit());
        }
        return ResponseEntity.ok(ProductOptionsDto.builder()
                .categories(new ArrayList<>(categories))
                .manufacturers(new ArrayList<>(manufacturers))
                .suppliers(new ArrayList<>(suppliers))
                .units(new ArrayList<>(units))
                .build());
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Integer> nextId() {
        return ResponseEntity.ok(productRepository.findMaxId() + 1);
    }

    @Transactional
    public ResponseEntity<ProductDraftDto> update(int productId, ProductDraftDto draft) {
        return save(copyDraft(productId, draft, draft.imagePath()));
    }

    @Transactional
    public ResponseEntity<ProductDraftDto> uploadImage(int productId, MultipartFile file) throws Exception {
        ProductDraftDto current = loadDraft(productId);
        String storedPath = imageStorageService.storeProductImage(file, productId, current.imagePath()).getBody();
        return save(copyDraft(productId, current, storedPath));
    }

    @Transactional
    public ResponseEntity<ProductDraftDto> save(ProductDraftDto draft) {
        validateDraft(draft);

        Product existing = productRepository.findById(draft.id()).orElse(null);
        LocalDateTime now = LocalDateTime.now();

        String article = existing != null && existing.getArticle() != null && !existing.getArticle().isBlank()
                ? existing.getArticle()
                : "P" + draft.id();

        Product product = Product.builder()
                .id(draft.id())
                .article(article)
                .name(draft.name().trim())
                .category(trimToNull(draft.category()))
                .description(draft.description() == null ? "" : draft.description().trim())
                .manufacturer(trimToNull(draft.manufacturer()))
                .supplier(trimToNull(draft.supplier()))
                .unit(trimToNull(draft.unit()))
                .price(draft.price())
                .stockQuantity(draft.stockQuantity())
                .discountPercent(draft.discountPercent() == null ? BigDecimal.ZERO : draft.discountPercent())
                .imagePath(draft.imagePath())
                .createdAt(existing != null ? existing.getCreatedAt() : now)
                .updatedAt(now)
                .build();

        productRepository.save(product);

        HttpStatus status = existing == null ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(loadDraft(product.getId()));
    }

    @Transactional
    public ResponseEntity<Void> delete(int productId) throws Exception {
        if (orderRepository.countByProduct_Id(productId) > 0) {
            throw new ProductDeleteException(ProductDeleteReason.LINKED_TO_ORDER);
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductDeleteException(ProductDeleteReason.NOT_FOUND));

        imageStorageService.deleteStoredImage(product.getImagePath());
        productRepository.delete(product);
        return ResponseEntity.noContent().build();
    }

    private ProductDraftDto loadDraft(int productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден."));
        return toDraft(product);
    }

    private ProductDraftDto copyDraft(int productId, ProductDraftDto draft, String imagePath) {
        return ProductDraftDto.builder()
                .id(productId)
                .name(draft.name())
                .category(draft.category())
                .description(draft.description())
                .manufacturer(draft.manufacturer())
                .supplier(draft.supplier())
                .unit(draft.unit())
                .price(draft.price())
                .stockQuantity(draft.stockQuantity())
                .discountPercent(draft.discountPercent())
                .imagePath(imagePath)
                .build();
    }

    private void validateDraft(ProductDraftDto draft) {
        if (draft.name() == null || draft.name().isBlank()) {
            throw new IllegalArgumentException("Укажите наименование товара.");
        }
        if (isBlank(draft.category()) || isBlank(draft.manufacturer()) || isBlank(draft.supplier()) || isBlank(draft.unit())) {
            throw new IllegalArgumentException("Заполните категорию, производителя, поставщика и единицу измерения.");
        }
        if (draft.price() == null || draft.stockQuantity() == null || draft.discountPercent() == null) {
            throw new IllegalArgumentException("Заполните цену, остаток и скидку.");
        }
    }

    private ProductListItemDto toListItem(Product product) {
        String article = product.getArticle();
        if (article == null || article.isBlank()) {
            article = "P" + product.getId();
        }

        return ProductListItemDto.builder()
                .id(product.getId())
                .article(article)
                .name(product.getName())
                .categoryName(product.getCategory())
                .description(product.getDescription())
                .manufacturerName(product.getManufacturer())
                .supplierName(product.getSupplier())
                .unitName(product.getUnit())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .discountPercent(product.getDiscountPercent())
                .imagePath(product.getImagePath())
                .build();
    }

    private ProductDraftDto toDraft(Product product) {
        return ProductDraftDto.builder()
                .id(product.getId())
                .name(product.getName())
                .category(product.getCategory())
                .description(product.getDescription())
                .manufacturer(product.getManufacturer())
                .supplier(product.getSupplier())
                .unit(product.getUnit())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .discountPercent(product.getDiscountPercent())
                .imagePath(product.getImagePath())
                .build();
    }

    private List<ProductListItemDto> filterBySearch(List<ProductListItemDto> products, String searchText) {
        if (searchText == null || searchText.isBlank()) {
            return products;
        }

        String normalized = searchText.trim().toLowerCase();
        List<ProductListItemDto> result = new ArrayList<>();
        for (ProductListItemDto product : products) {
            if (matchesSearch(product, normalized)) {
                result.add(product);
            }
        }
        return result;
    }

    private boolean matchesSearch(ProductListItemDto product, String normalized) {
        String[] fields = {
                product.article(),
                product.name(),
                product.categoryName(),
                product.description(),
                product.manufacturerName(),
                product.supplierName(),
                product.unitName(),
                String.valueOf(product.id()),
        };

        for (String value : fields) {
            if (value != null && value.toLowerCase().contains(normalized)) {
                return true;
            }
        }
        return false;
    }

    private List<ProductListItemDto> filterBySupplier(List<ProductListItemDto> products, String supplierFilter) {
        if (supplierFilter == null || supplierFilter.isBlank() || "ALL".equalsIgnoreCase(supplierFilter)) {
            return products;
        }

        String normalized = supplierFilter.trim();
        List<ProductListItemDto> result = new ArrayList<>();
        for (ProductListItemDto product : products) {
            if (product.supplierName() != null
                    && product.supplierName().equalsIgnoreCase(normalized)) {
                result.add(product);
            }
        }
        return result;
    }

    private List<ProductListItemDto> filterByDiscount(List<ProductListItemDto> products, String discountFilter) {
        if (discountFilter == null || discountFilter.isBlank() || "ALL".equalsIgnoreCase(discountFilter)) {
            return products;
        }

        String normalizedFilter = discountFilter.toUpperCase();
        if (!"UP_TO_12_99".equals(normalizedFilter)
                && !"FROM_13_TO_16_99".equals(normalizedFilter)
                && !"FROM_17".equals(normalizedFilter)) {
            return products;
        }

        List<ProductListItemDto> result = new ArrayList<>();
        for (ProductListItemDto product : products) {
            if (matchesDiscountFilter(product, normalizedFilter)) {
                result.add(product);
            }
        }
        return result;
    }

    private boolean matchesDiscountFilter(ProductListItemDto product, String discountFilter) {
        BigDecimal discount = product.discountPercent();

        if ("UP_TO_12_99".equals(discountFilter)) {
            return discount.compareTo(BigDecimal.ZERO) >= 0
                    && discount.compareTo(new BigDecimal("13")) < 0;
        }
        if ("FROM_13_TO_16_99".equals(discountFilter)) {
            return discount.compareTo(new BigDecimal("13")) >= 0
                    && discount.compareTo(new BigDecimal("17")) < 0;
        }
        if ("FROM_17".equals(discountFilter)) {
            return discount.compareTo(new BigDecimal("17")) >= 0;
        }
        return true;
    }

    private List<ProductListItemDto> sortProducts(
            List<ProductListItemDto> products,
            String sortField,
            String sortDirection
    ) {
        List<ProductListItemDto> sorted = new ArrayList<>(products);
        String normalizedSortField = sortField == null ? "NONE" : sortField.toUpperCase();

        sorted.sort((left, right) -> compareProducts(left, right, normalizedSortField));

        if ("DESC".equalsIgnoreCase(sortDirection)
                && sortField != null
                && !"NONE".equalsIgnoreCase(sortField)) {
            Collections.reverse(sorted);
        }
        return sorted;
    }

    private int compareProducts(ProductListItemDto left, ProductListItemDto right, String sortField) {
        if ("PRICE".equals(sortField)) {
            int priceCompare = left.price().compareTo(right.price());
            if (priceCompare != 0) {
                return priceCompare;
            }
            return left.name().compareToIgnoreCase(right.name());
        }

        if ("STOCK".equals(sortField)) {
            int stockCompare = Integer.compare(left.stockQuantity(), right.stockQuantity());
            if (stockCompare != 0) {
                return stockCompare;
            }
            return left.name().compareToIgnoreCase(right.name());
        }

        int idCompare = Integer.compare(left.id(), right.id());
        if (idCompare != 0) {
            return idCompare;
        }
        return left.name().compareToIgnoreCase(right.name());
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void addDistinctValue(TreeSet<String> values, String value) {
        if (value != null && !value.trim().isEmpty()) {
            values.add(value.trim());
        }
    }

    public enum ProductDeleteReason {
        LINKED_TO_ORDER,
        NOT_FOUND
    }

    public static class ProductDeleteException extends Exception {
        private final ProductDeleteReason reason;

        public ProductDeleteException(ProductDeleteReason reason) {
            super(reason.name());
            this.reason = reason;
        }

        public ProductDeleteReason getReason() {
            return reason;
        }
    }
}
