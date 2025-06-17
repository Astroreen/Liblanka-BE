package me.astroreen.liblanka.domain.product.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.astroreen.liblanka.domain.product.dto.ProductConstructionInfoDto;
import me.astroreen.liblanka.domain.product.dto.ProductVariantDto;
import me.astroreen.liblanka.domain.product.entity.Product;
import me.astroreen.liblanka.domain.product.entity.ProductColor;
import me.astroreen.liblanka.domain.product.entity.ProductSize;
import me.astroreen.liblanka.domain.product.entity.ProductVariant;
import me.astroreen.liblanka.domain.product.repository.ProductColorRepository;
import me.astroreen.liblanka.domain.product.repository.ProductSizeRepository;
import me.astroreen.liblanka.domain.product.repository.ProductVariantRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductVariantService {
    private final ProductColorRepository productColorRepository;
    private final ProductSizeRepository productSizeRepository;
    private final ProductVariantRepository productVariantRepository;


    @Transactional
    public Product parseJsonProductVariants(Product originalProduct, String jsonVariants) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<ProductVariantDto> variants = parseJsonProductVariants(mapper, jsonVariants);

        List<ProductVariant> productVariants = new ArrayList<>(variants.size());
        for (ProductVariantDto variant : variants) {
            ProductVariant productVariant = ProductVariant.builder()
                    .product(originalProduct)
                    .color(productColorRepository.findById(variant.getColorId())
                            .orElseThrow(() -> new IllegalArgumentException("Invalid product color ID")))
                    .size(productSizeRepository.findById(variant.getSizeId())
                            .orElseThrow(() -> new IllegalArgumentException("Invalid product size ID")))
                    .quantity(variant.getQuantity())
                    .build();

            productVariants.add(productVariant);
        }
        productVariantRepository.saveAll(productVariants);
        originalProduct.setVariants(productVariants);
        return originalProduct;
    }

    private @NotNull List<ProductVariantDto> parseJsonProductVariants(@NotNull ObjectMapper objectMapper, String jsonVariants) throws IOException {
        List<ProductVariantDto> variants = new ArrayList<>();

        // Parse JSON string into JsonNode
        JsonNode jsonArray = objectMapper.readTree(jsonVariants);

        // Check if the JSON is an array
        if (!jsonArray.isArray()) {
            throw new IllegalArgumentException("jsonVariants must be a JSON array");
        }

        // Iterate over each element in the JSON array
        for (JsonNode node : jsonArray) {
            // Manually extract fields
            Long colorId = node.has("colorId") ? node.get("colorId").asLong() : null;
            Long sizeId = node.has("sizeId") ? node.get("sizeId").asLong() : null;
            Integer quantity = node.has("quantity") ? node.get("quantity").asInt() : null;

            // Validate required fields
            if (colorId == null || sizeId == null || quantity == null) {
                throw new IllegalArgumentException("Invalid ProductVariantDto: missing required fields");
            }

            // Build ProductVariantDto
            ProductVariantDto variant = ProductVariantDto.builder()
                    .colorId(colorId)
                    .sizeId(sizeId)
                    .quantity(quantity)
                    .build();
            variants.add(variant);
        }

        return variants;
    }
}
