package me.astroreen.liblanka.domain.product.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.astroreen.liblanka.domain.product.entity.ProductColor;
import me.astroreen.liblanka.domain.product.repository.ProductColorRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductColorService {

    private final ProductColorRepository productColorRepository;
    private final ProductService productService;

    public @NotNull List<ProductColor> findAll() {
        return productColorRepository.findAll();
    }

    public @NotNull ProductColor create(@NotNull String name, @NotNull String hex) throws IllegalArgumentException, OptimisticLockingFailureException {
        return productColorRepository.save(ProductColor.builder().name(name).hex(hex).build());
    }

    @Transactional
    public void delete(@NotNull ProductColor colorToDelete, @NotNull ProductColor replacementColor) 
            throws IllegalArgumentException, OptimisticLockingFailureException {
        // Update all product variants that use the color to be deleted
        productService.updateProductColors(colorToDelete.getId(), replacementColor.getId());
        
        // Delete the original color
        productColorRepository.delete(colorToDelete);
    }
}
