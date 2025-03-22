package me.astroreen.liblanka.domain.product.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import me.astroreen.liblanka.domain.product.entity.Attribute;
import me.astroreen.liblanka.domain.product.entity.ProductColor;
import me.astroreen.liblanka.domain.product.entity.ProductSize;
import me.astroreen.liblanka.domain.product.entity.ProductType;

import java.util.List;

@Getter
@Setter
@Builder
public class ProductAdminInformation {
    private List<ProductSize> sizes;
    private List<ProductColor> colors;
    private List<ProductType> types;
    private List<Attribute> attributes;
}
