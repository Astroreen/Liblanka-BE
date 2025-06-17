package me.astroreen.liblanka.domain.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCardDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageData; // base64 encoded image data
} 