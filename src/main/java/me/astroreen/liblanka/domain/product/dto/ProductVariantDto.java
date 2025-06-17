package me.astroreen.liblanka.domain.product.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ProductVariantDto {
    private Long colorId;
    private Long sizeId;
    private Integer quantity;
}
