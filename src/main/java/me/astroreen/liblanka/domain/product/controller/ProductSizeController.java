package me.astroreen.liblanka.domain.product.controller;

import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.astroreen.liblanka.domain.product.entity.ProductSize;
import me.astroreen.liblanka.domain.product.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/storage/products/sizes")
@RequiredArgsConstructor
public class ProductSizeController {

    private @NonNull ProductService productService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<List<ProductSize>> getAllProductSizes() {
        return ResponseEntity.ok(productService.getAllProductSizes());
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public void createProductSizes(@RequestBody @Valid List<String> names){
        productService.createProductSizes(names);
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping
    public void deleteProductSize(
            @RequestParam(name = "delete") String delete,
            @RequestParam(name = "replace") String replace) {
        productService.deleteProductSize(delete, replace);
    }
}
