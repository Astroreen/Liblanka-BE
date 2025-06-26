package me.astroreen.liblanka.domain.product.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import me.astroreen.liblanka.domain.product.entity.ProductColor;
import me.astroreen.liblanka.domain.product.entity.ProductSize;
import me.astroreen.liblanka.domain.product.entity.ProductType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
public class ProductDto {

    private Long id;
    private String name;
    private Long typeId;
    private String typeName;
    private String description;
    private BigDecimal price;
    private List<String> attributes;
    private List<String> imageData; // base64 images
    private Map<Long, List<String>> imagesByColor; // colorId -> images
    private List<ProductVariantDto> variants;
    private Map<Long, List<ProductVariantDto>> variantsByColor; // colorId -> variants
    private List<ProductColor> colors;
    private List<ProductSize> sizes;
    private ProductType type;
}
