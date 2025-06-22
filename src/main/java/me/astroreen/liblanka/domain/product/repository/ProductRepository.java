package me.astroreen.liblanka.domain.product.repository;

import me.astroreen.liblanka.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    
    List<Product> findByTypeId(Long typeId);
    
    @Query("SELECT DISTINCT p FROM Product p JOIN p.variants v WHERE v.color.id = :colorId")
    List<Product> findByColorId(@Param("colorId") Long colorId);
    
    @Query("SELECT DISTINCT p FROM Product p JOIN p.variants v WHERE v.size.id = :sizeId")
    List<Product> findBySizeId(@Param("sizeId") Long sizeId);
}
