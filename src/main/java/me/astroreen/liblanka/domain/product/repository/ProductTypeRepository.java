package me.astroreen.liblanka.domain.product.repository;

import me.astroreen.liblanka.domain.product.entity.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductTypeRepository extends JpaRepository<ProductType, Long> {
}
