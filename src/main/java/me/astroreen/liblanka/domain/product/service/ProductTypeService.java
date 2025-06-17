package me.astroreen.liblanka.domain.product.service;

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

    public List<ProductType> findAll() {
        return productTypeRepository.findAll();
    }

    public @NotNull ProductType create(@NotNull String name) throws IllegalArgumentException, OptimisticLockingFailureException {
        return productTypeRepository.save(ProductType.builder().name(name).build());
    }

    public void delete(@NotNull ProductType type) throws IllegalArgumentException, OptimisticLockingFailureException {
        productTypeRepository.delete(type);
    }
}
