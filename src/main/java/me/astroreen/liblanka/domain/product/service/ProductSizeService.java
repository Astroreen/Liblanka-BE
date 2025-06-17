package me.astroreen.liblanka.domain.product.service;

import lombok.RequiredArgsConstructor;
import me.astroreen.liblanka.domain.product.entity.ProductSize;
import me.astroreen.liblanka.domain.product.entity.ProductType;
import me.astroreen.liblanka.domain.product.repository.ProductSizeRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductSizeService {

    private final ProductSizeRepository productSizeRepository;

    public List<ProductSize> findAll() {
        return productSizeRepository.findAll();
    }

    public @NotNull ProductSize create(@NotNull String name) throws IllegalArgumentException, OptimisticLockingFailureException {
        return productSizeRepository.save(ProductSize.builder().name(name).build());
    }

    public void delete(@NotNull ProductSize type) throws IllegalArgumentException, OptimisticLockingFailureException {
        productSizeRepository.delete(type);
    }
}
