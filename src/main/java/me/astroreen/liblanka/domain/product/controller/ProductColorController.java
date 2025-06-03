package me.astroreen.liblanka.domain.product.controller;

import lombok.RequiredArgsConstructor;
import me.astroreen.liblanka.domain.product.entity.ProductType;
import me.astroreen.liblanka.domain.product.service.ProductTypeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/storage/products/types")
@RequiredArgsConstructor
public class ProductTypeController {

    private final ProductTypeService productTypeService;

    @GetMapping
    public ResponseEntity<List<ProductType>> getAllProductTypes() {
        return ResponseEntity.ok(productTypeService.findAll());
    }

    @PostMapping
    @PreAuthorize("isAuthenticated() and hasRole('ADMIN')")
    public ResponseEntity<ProductType> createNewProductType(@RequestParam(name = "name") String name) {
        final ProductType type;
        try {
            type = productTypeService.create(name);
            return ResponseEntity.status(HttpStatus.CREATED).body(type);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping
    @PreAuthorize("isAuthenticated() and hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProductType(@RequestBody ProductType type){
        if(type == null) return ResponseEntity.badRequest().build();
        productTypeService.delete(type);
        return ResponseEntity.noContent().build();
    }
}
