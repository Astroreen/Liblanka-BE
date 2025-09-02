package me.astroreen.liblanka.domain.product.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ProductImageDto {
    private Long id;
    private Long productId;
    private Long colorId;
    private String imageId;
}
