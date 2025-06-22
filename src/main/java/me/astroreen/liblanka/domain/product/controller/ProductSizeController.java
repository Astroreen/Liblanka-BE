package me.astroreen.liblanka.domain.product.controller;

import lombok.RequiredArgsConstructor;
import me.astroreen.liblanka.domain.product.dto.ReplacementRequest;
import me.astroreen.liblanka.domain.product.entity.ProductSize;
import me.astroreen.liblanka.domain.product.service.ProductSizeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/storage/products/sizes")
@RequiredArgsConstructor
public class ProductSizeController {

    private final ProductSizeService productSizeService;

    @GetMapping
    public ResponseEntity<List<ProductSize>> getAllProductSizes() {
        return ResponseEntity.ok(productSizeService.findAll());
    }

    @PostMapping
    @PreAuthorize("isAuthenticated() and hasRole('ADMIN')")
    public ResponseEntity<ProductSize> createNewProductSize(@RequestParam(name = "name") String name) {
        final ProductSize type;
        try {
            type = productSizeService.create(name);
            return ResponseEntity.status(HttpStatus.CREATED).body(type);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping
    @PreAuthorize("isAuthenticated() and hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProductSizeWithReplacement(@RequestBody ReplacementRequest<ProductSize> request){
        if(request == null || request.getItemToDelete() == null || request.getReplacementItem() == null) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            productSizeService.delete(request.getItemToDelete(), request.getReplacementItem());
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
