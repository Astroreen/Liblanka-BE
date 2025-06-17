package me.astroreen.liblanka.domain.product.controller;

import lombok.RequiredArgsConstructor;
import me.astroreen.liblanka.domain.product.entity.ProductColor;
import me.astroreen.liblanka.domain.product.service.ProductColorService;
import me.astroreen.liblanka.domain.product.util.ColorValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/storage/products/colors")
@RequiredArgsConstructor
public class ProductColorController {

    private final ProductColorService productColorService;

    @GetMapping
    public ResponseEntity<List<ProductColor>> getAllProductColors() {
        return ResponseEntity.ok(productColorService.findAll());
    }

    @PostMapping
    @PreAuthorize("isAuthenticated() and hasRole('ADMIN')")
    public ResponseEntity<ProductColor> createNewProductColor(@RequestParam(name = "name") String name, @RequestParam(name = "hex") String hex) {
        if(name == null || name.isBlank()) return ResponseEntity.badRequest().build();
        if(hex == null || !ColorValidator.isValidHexColor(hex)) return ResponseEntity.badRequest().build();

        final ProductColor type;
        try {
            type = productColorService.create(name, hex);
            return ResponseEntity.status(HttpStatus.CREATED).body(type);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping
    @PreAuthorize("isAuthenticated() and hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProductColor(@RequestBody ProductColor color){
        if(color == null) return ResponseEntity.badRequest().build();
        productColorService.delete(color);
        return ResponseEntity.noContent().build();
    }
}
