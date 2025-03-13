package me.astroreen.liblanka.domain.product.controller;

import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.astroreen.liblanka.domain.product.entity.ProductType;
import me.astroreen.liblanka.domain.product.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/storage/products/types")
@RequiredArgsConstructor
public class ProductTypeController {

    private @NonNull ProductService productService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<List<ProductType>> getAllProductTypes() {
        return ResponseEntity.ok(productService.getAllProductTypes());
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public void createProductTypes(@RequestBody @Valid List<String> names){
        productService.createProductTypes(names);
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping
    public void deleteProductType(
            @RequestParam(name = "delete") String delete,
            @RequestParam(name = "replace") String replace) {
        productService.deleteProductType(delete, replace);
    }
}
