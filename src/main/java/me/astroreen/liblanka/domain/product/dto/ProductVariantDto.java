package me.astroreen.liblanka.domain.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantDto {
    private Long colorId;
    private Long sizeId;
    private Integer quantity;
}
