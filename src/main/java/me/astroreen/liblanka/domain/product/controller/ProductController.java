package me.astroreen.liblanka.domain.product.controller;

import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.astroreen.liblanka.domain.product.dto.ProductAdminInformation;
import me.astroreen.liblanka.domain.product.entity.Product;
import me.astroreen.liblanka.domain.product.repository.specification.ProductSpecifications;
import me.astroreen.liblanka.domain.product.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/storage/products")
@RequiredArgsConstructor
public class ProductController {

    private static final int MAX_PAGE_SIZE = 50;
    private @NonNull ProductService service;

    @GetMapping
    public ResponseEntity<Page<Product>> getProductsWithFilterOption(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "price-from", required = false) BigDecimal priceFrom,
            @RequestParam(name = "price-to", required = false) BigDecimal priceTo,
            @RequestParam(name = "quantity-from", required = false) Integer quantityFrom,
            @RequestParam(name = "quantity-to", required = false) Integer quantityTo,
            @RequestParam(name = "color-id", required = false) List<Long> colorIds,
            @RequestParam(name = "size-id", required = false) List<Long> sizeIds,
            @RequestParam(name = "attribute-id", required = false) List<Long> attributeIds,
            @RequestParam(name = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(name = "page-size", required = false, defaultValue = "20") Integer pageSize
    ) {

        if (page <= 0) {
            return ResponseEntity.badRequest().build();
        }

        if (pageSize < 0 || pageSize > MAX_PAGE_SIZE) {
            return ResponseEntity.badRequest().build();
        }

        if ((priceFrom != null && priceFrom.doubleValue() < 0) ||
                (priceFrom != null && priceTo != null && priceTo.compareTo(priceFrom) < 0)) {
            return ResponseEntity.badRequest().build();
        }

        if ((quantityFrom != null && quantityFrom < 0) ||
                (quantityFrom != null && quantityTo != null && quantityTo < quantityFrom)) {
            return ResponseEntity.badRequest().build();
        }

        var filter = new ProductSpecifications.Filter(
                name,
                priceFrom,
                priceTo,
                quantityFrom,
                quantityTo,
                colorIds,
                sizeIds,
                attributeIds
        );

        return ResponseEntity.ofNullable(service.filter(filter, PageRequest.of(--page, pageSize)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ofNullable(service.getProductById(id));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/all-information")
    public ResponseEntity<ProductAdminInformation> getAllAdminProductInformation() {
        ProductAdminInformation info = ProductAdminInformation.builder()
                .types(service.getAllProductTypes())
                .sizes(service.getAllProductSizes())
                .colors(service.getAllProductColors())
                .attributes(service.getAllProductAttributes())
                .build();
        return ResponseEntity.ok(info);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<List<Product>> createProducts(@RequestBody @Valid List<ProductService.ProductRequest> productRequests) {
        List<Product> createdProducts = service.createProducts(productRequests);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProducts);
    }
}
