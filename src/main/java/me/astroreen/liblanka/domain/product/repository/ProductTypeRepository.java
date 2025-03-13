package me.astroreen.liblanka.domain.product.repository;

import me.astroreen.liblanka.domain.product.entity.ProductType;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductTypeRepository extends JpaRepository<ProductType, Long> {
    Optional<ProductType> findByName(@NotNull String name);
}
