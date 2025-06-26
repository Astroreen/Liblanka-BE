package me.astroreen.liblanka.domain.product.service;

import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import me.astroreen.liblanka.domain.product.dto.ProductConstructionInfoDto;
import me.astroreen.liblanka.domain.product.dto.ProductDto;
import me.astroreen.liblanka.domain.product.dto.ProductVariantDto;
import me.astroreen.liblanka.domain.product.entity.Product;
import me.astroreen.liblanka.domain.product.entity.ProductColor;
import me.astroreen.liblanka.domain.product.entity.ProductSize;
import me.astroreen.liblanka.domain.product.entity.ProductType;
import me.astroreen.liblanka.domain.product.entity.ProductVariant;
import me.astroreen.liblanka.domain.product.repository.ProductColorRepository;
import me.astroreen.liblanka.domain.product.repository.ProductRepository;
import me.astroreen.liblanka.domain.product.repository.ProductSizeRepository;
import me.astroreen.liblanka.domain.product.repository.ProductTypeRepository;
import me.astroreen.liblanka.domain.product.repository.ProductVariantRepository;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Base64;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductTypeRepository productTypeRepository;
    private final ProductColorRepository productColorRepository;
    private final ProductSizeRepository productSizeRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductImageService productImageService;

    public Product findById(@NotNull Long id) throws NoSuchElementException {
        return productRepository.findById(id).orElseThrow(() -> new NoSuchElementException("There was not such product in database with id " + id));
    }

    @Transactional
    public Product createProduct(@NotNull @NotBlank String name,
                                 @NotNull Long typeId,
                                 @Nullable @NotBlank String description,
                                 @NotNull BigDecimal price,
                                 @Nullable @NotBlank List<String> attributes)
            throws IllegalArgumentException {



        Product product = Product.builder()
                .name(name)
                .type(productTypeRepository.findById(typeId)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid type ID")))
                .description(description)
                .price(price)
                .attributes(attributes) // JSONB
                .build();

        return productRepository.save(product);
    }

    public void deleteProduct(@NotNull Long id) throws NoSuchElementException {
        Product product = findById(id);
        if (product != null) productRepository.delete(product);
    }

    public ProductConstructionInfoDto getProductConstructionInfo() {
        List<ProductColor> colors = productColorRepository.findAll();
        List<ProductSize> sizes = productSizeRepository.findAll();
        List<ProductType> types = productTypeRepository.findAll();

        return ProductConstructionInfoDto.builder().sizes(sizes).colors(colors).types(types).build();
    }

    public Page<Product> findAll(Specification<Product> spec, Pageable pageable) {
        return productRepository.findAll(spec, pageable);
    }

    @Transactional
    public void updateProductTypes(Long oldTypeId, Long newTypeId) {
        ProductType newType = productTypeRepository.findById(newTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid replacement type ID"));
        
        List<Product> productsToUpdate = productRepository.findByTypeId(oldTypeId);
        for (Product product : productsToUpdate) {
            product.setType(newType);
        }
        productRepository.saveAll(productsToUpdate);
    }

    @Transactional
    public void updateProductSizes(Long oldSizeId, Long newSizeId) {
        productSizeRepository.findById(newSizeId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid replacement size ID"));
        
        // Update product variants that use the old size
        productVariantRepository.updateSizeId(oldSizeId, newSizeId);
    }

    @Transactional
    public ProductDto getProductDetails(Long id) {
        Product product = findById(id);
        // Map images
        List<String> imageData = product.getImages() != null && product.getImages().getFirst().getColor() == null ? 
            product.getImages().stream()
                .map(img -> Base64.getEncoder().encodeToString(img.getImageData()))
                .collect(Collectors.toList()) : List.of();
        // Map images by color
        Map<Long, List<String>> imagesByColor = new HashMap<>();
        if (product.getImages() != null) {
            product.getImages().forEach(img -> {
                if (img.getColor() != null) {
                    imagesByColor.computeIfAbsent(img.getColor().getId(), k -> new java.util.ArrayList<>())
                        .add(Base64.getEncoder().encodeToString(img.getImageData()));
                }
            });
        }
        // Map variants
        List<ProductVariantDto> variants = product.getVariants() != null ? product.getVariants().stream()
            .map(v -> ProductVariantDto.builder()
                .colorId(v.getColor().getId())
                .sizeId(v.getSize().getId())
                .quantity(v.getQuantity())
                .build())
            .collect(Collectors.toList()) : List.of();
        // Map variants by color
        Map<Long, List<ProductVariantDto>> variantsByColor = new HashMap<>();
        if (product.getVariants() != null) {
            product.getVariants().forEach(v -> {
                if (v.getColor() != null) {
                    variantsByColor.computeIfAbsent(v.getColor().getId(), k -> new java.util.ArrayList<>())
                        .add(ProductVariantDto.builder()
                            .colorId(v.getColor().getId())
                            .sizeId(v.getSize().getId())
                            .quantity(v.getQuantity())
                            .build());
                }
            });
        }
        // Map colors
        List<ProductColor> colors = product.getVariants() != null ? product.getVariants().stream()
            .map(v -> v.getColor())
            .distinct()
            .map(c -> ProductColor.builder().id(c.getId()).name(c.getName()).hex(c.getHex()).build())
            .collect(Collectors.toList()) : List.of();
        // Map sizes
        List<ProductSize> sizes = product.getVariants() != null ? product.getVariants().stream()
            .map(v -> v.getSize())
            .distinct()
            .map(s -> ProductSize.builder().id(s.getId()).name(s.getName()).build())
            .collect(Collectors.toList()) : List.of();
        // Map type
        ProductType type = product.getType() != null ? ProductType.builder().id(product.getType().getId()).name(product.getType().getName()).build() : null;
        return ProductDto.builder()
            .id(product.getId())
            .name(product.getName())
            .typeId(product.getType().getId())
            .typeName(product.getType().getName())
            .description(product.getDescription())
            .price(product.getPrice())
            .attributes(product.getAttributes())
            .imageData(imageData)
            .imagesByColor(imagesByColor)
            .variants(variants)
            .variantsByColor(variantsByColor)
            .colors(colors)
            .sizes(sizes)
            .type(type)
            .build();
    }

    @Transactional
    public ProductDto updateProduct(Long id, ProductDto dto) {
        Product product = findById(id);
        if (dto.getName() != null) product.setName(dto.getName());
        if (dto.getDescription() != null) product.setDescription(dto.getDescription());
        if (dto.getPrice() != null) product.setPrice(dto.getPrice());
        if (dto.getTypeId() != null) {
            product.setType(productTypeRepository.findById(dto.getTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid type ID")));
        }
        if (dto.getAttributes() != null) product.setAttributes(dto.getAttributes());
        // TODO: update variants and images if needed
        productRepository.save(product);
        return getProductDetails(id);
    }

    @Transactional
    public ProductDto updateProductMultipart(Long id, String name, String typeId, String description, String price, String jsonProductAttributes, String jsonProductVariants, String existingImageBindings, List<MultipartFile> newImages, String jsonNewImageColorIds) 
        throws Exception {
            
        Product product = findById(id);
        // Update fields
        if (name != null) product.setName(name);
        if (description != null) product.setDescription(description);
        if (price != null) product.setPrice(new java.math.BigDecimal(price));
        if (typeId != null) product.setType(productTypeRepository.findById(Long.parseLong(typeId)).orElseThrow());
        ObjectMapper mapper = new ObjectMapper();
        if (jsonProductAttributes != null) product.setAttributes(mapper.readValue(jsonProductAttributes, new TypeReference<List<String>>(){}));
        // Update variants
        if (jsonProductVariants != null) {
            product.getVariants().clear();
            List<ProductVariantDto> variants = mapper.readValue(jsonProductVariants, new TypeReference<List<ProductVariantDto>>(){});
            for (ProductVariantDto v : variants) {
                ProductVariant variant = ProductVariant.builder()
                .product(product)
                .color(productColorRepository.findById(v.getColorId()).orElseThrow())
                .size(productSizeRepository.findById(v.getSizeId()).orElseThrow())
                .quantity(v.getQuantity())
                .build();
                product.getVariants().add(variant);
            }
        }
        // Handle images
        // 1. Update color bindings for existing images
        if (product.getImages() != null && existingImageBindings != null && !existingImageBindings.isBlank()) {
            Map<String, Integer> bindings = mapper.readValue(existingImageBindings, new TypeReference<Map<String, Integer>>() {});
            for (var entry : bindings.entrySet()) {
                String key = entry.getKey();
                Integer colorId = entry.getValue();
                if (key.startsWith("id-")) {
                    Long imageId = Long.parseLong(key.substring(3));
                    var imgOpt = product.getImages().stream().filter(img -> img.getId().equals(imageId)).findFirst();
                    if (imgOpt.isPresent()) {
                        var img = imgOpt.get();
                        if (colorId != null) {
                            img.setColor(productColorRepository.findById(Long.valueOf(colorId)).orElse(null));
                        } else {
                            img.setColor(null);
                        }
                    }
                }
                // handle new images if needed
            }
        }
        // 2. Add new images with color bindings
        if (newImages != null && jsonNewImageColorIds != null) {
            for (int i = 0; i < newImages.size(); i++) {
                MultipartFile file = newImages.get(i);
                List<String> newImageColorIds = mapper.readValue(jsonNewImageColorIds, new TypeReference<List<String>>(){});
                String colorIdStr = newImageColorIds.get(i);
                Long colorId = null;
                if (colorIdStr != null && !colorIdStr.equals("null") && !colorIdStr.isEmpty()) {
                    colorId = Long.valueOf(mapper.readValue(colorIdStr, Integer.class));
                }

                try {
                    productImageService.saveImageWithColor(product, file, colorId);
                } catch (IOException e) {
                    // 
                }
            }
        }
        // Save product
        productRepository.save(product);
        return getProductDetails(id);
    }
}
