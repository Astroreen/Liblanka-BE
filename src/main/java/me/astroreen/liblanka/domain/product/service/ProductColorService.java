package me.astroreen.liblanka.domain.product.service;

import lombok.RequiredArgsConstructor;
import me.astroreen.liblanka.domain.product.entity.ProductColor;
import me.astroreen.liblanka.domain.product.repository.ProductColorRepository;
import me.astroreen.liblanka.domain.product.util.ColorValidator;
import org.jetbrains.annotations.NotNull;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductColorService {

    private final ProductColorRepository productColorRepository;

    public @NotNull List<ProductColor> findAll() {
        return productColorRepository.findAll();
    }

    public @NotNull ProductColor create(@NotNull String name, @NotNull String hex) throws IllegalArgumentException, OptimisticLockingFailureException {
        return productColorRepository.save(ProductColor.builder().name(name).hex(hex).build());
    }

    public void delete(@NotNull ProductColor type) throws IllegalArgumentException, OptimisticLockingFailureException {
        productColorRepository.delete(type);
    }
}
