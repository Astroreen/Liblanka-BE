package me.astroreen.liblanka.domain.product.repository;

import me.astroreen.liblanka.domain.product.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    
    List<ProductVariant> findByColorId(Long colorId);
    
    List<ProductVariant> findBySizeId(Long sizeId);
    
    @Modifying
    @Query("UPDATE ProductVariant v SET v.color.id = :newColorId WHERE v.color.id = :oldColorId")
    void updateColorId(@Param("oldColorId") Long oldColorId, @Param("newColorId") Long newColorId);
    
    @Modifying
    @Query("UPDATE ProductVariant v SET v.size.id = :newSizeId WHERE v.size.id = :oldSizeId")
    void updateSizeId(@Param("oldSizeId") Long oldSizeId, @Param("newSizeId") Long newSizeId);
}
