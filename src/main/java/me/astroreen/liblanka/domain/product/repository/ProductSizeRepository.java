package me.astroreen.liblanka.domain.product.repository;

import me.astroreen.liblanka.domain.product.entity.ProductSize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductSizeRepository extends JpaRepository<ProductSize, Long> {
}
