package me.astroreen.liblanka.domain.product.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.astroreen.liblanka.domain.product.entity.ProductType;
import me.astroreen.liblanka.domain.product.repository.ProductTypeRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductTypeService {

    private final ProductTypeRepository productTypeRepository;
    private final ProductService productService;

    public List<ProductType> findAll() {
        return productTypeRepository.findAll();
    }

    public @NotNull ProductType create(@NotNull String name) throws IllegalArgumentException, OptimisticLockingFailureException {
        return productTypeRepository.save(ProductType.builder().name(name).build());
    }

    @Transactional
    public void delete(@NotNull ProductType typeToDelete, @NotNull ProductType replacementType) 
            throws IllegalArgumentException, OptimisticLockingFailureException {
        // Update all products that use the type to be deleted
        productService.updateProductTypes(typeToDelete.getId(), replacementType.getId());
        
        // Delete the original type
        productTypeRepository.delete(typeToDelete);
    }
}
