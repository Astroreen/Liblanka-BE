package me.astroreen.liblanka.domain.product.repository;

import jakarta.transaction.Transactional;
import me.astroreen.liblanka.domain.product.entity.Product;
import me.astroreen.liblanka.domain.product.entity.ProductType;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.type = :to WHERE p.type = :from")
    void updateProductTypeValue(@Param("from") ProductType replaceFrom, @Param("to") ProductType replaceTo);

    Page<Product> findAll(@NotNull Specification<Product> productSpecification, Pageable pageable);
}
