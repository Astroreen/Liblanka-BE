package me.astroreen.liblanka.domain.product.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ImageMetadataDto {
    private String key; // Key to match with the file in multipart
    private Long colorId;
}
