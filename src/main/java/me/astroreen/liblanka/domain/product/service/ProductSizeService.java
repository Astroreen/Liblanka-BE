package me.astroreen.liblanka.domain.product.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.astroreen.liblanka.domain.product.entity.ProductSize;
import me.astroreen.liblanka.domain.product.repository.ProductSizeRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductSizeService {

    private final ProductSizeRepository productSizeRepository;
    private final ProductService productService;

    public List<ProductSize> findAll() {
        return productSizeRepository.findAll();
    }

    public @NotNull ProductSize create(@NotNull String name) throws IllegalArgumentException, OptimisticLockingFailureException {
        return productSizeRepository.save(ProductSize.builder().name(name).build());
    }

    @Transactional
    public void delete(@NotNull ProductSize sizeToDelete, @NotNull ProductSize replacementSize) 
            throws IllegalArgumentException, OptimisticLockingFailureException {
        // Update all product variants that use the size to be deleted
        productService.updateProductSizes(sizeToDelete.getId(), replacementSize.getId());
        
        // Delete the original size
        productSizeRepository.delete(sizeToDelete);
    }
}
