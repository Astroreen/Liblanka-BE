package me.astroreen.liblanka.domain.product.repository;

import me.astroreen.liblanka.domain.product.entity.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductTypeRepository extends JpaRepository<ProductType, Long> {
}
