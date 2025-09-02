package me.astroreen.liblanka.domain.product.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.astroreen.liblanka.domain.product.dto.ImageMetadataDto;
import me.astroreen.liblanka.domain.product.dto.ProductCardDto;
import me.astroreen.liblanka.domain.product.dto.ProductConstructionInfoDto;
import me.astroreen.liblanka.domain.product.dto.ProductDto;
import me.astroreen.liblanka.domain.product.entity.Product;
import me.astroreen.liblanka.domain.product.entity.specifications.ProductSpecifications;
import me.astroreen.liblanka.domain.product.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Arrays;

@RestController
@RequestMappingds("/storage/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final Logger logger = Logger.getLogger(getClass().getName());

    @GetMapping("/information")
    public ResponseEntity<ProductConstructionInfoDto> getProductConstructionInfo() {
        ProductConstructionInfoDto answer = productService.getProductConstructionInfo();
        return ResponseEntity.ok(answer);
    }

    @Transactional
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProduct(@PathVariable Long id) {
        if(id == null || id < 0) return ResponseEntity.badRequest().build();
        try {
            ProductDto dto = productService.getProductDetails(id);
            return ResponseEntity.ok(dto);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Transactional
    @GetMapping("/filter")
    public ResponseEntity<Page<ProductCardDto>> filterProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long typeId,
            @RequestParam(required = false) List<Long> sizeIds,
            @RequestParam(required = false) List<Long> colorIds,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        // Validate input parameters
        if (size <= 0 || size > 100) size = 20;
        if (page < 0) page = 0;
        if (typeId != null && typeId < 0) return ResponseEntity.badRequest().build();
        if (sizeIds != null && sizeIds.stream().anyMatch(id -> id < 0)) return ResponseEntity.badRequest().build();
        if (colorIds != null && colorIds.stream().anyMatch(id -> id < 0)) return ResponseEntity.badRequest().build();
        if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) < 0) return ResponseEntity.badRequest().build();
        if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) < 0) return ResponseEntity.badRequest().build();
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            return ResponseEntity.badRequest().build();
        }

        // Trim name (forgive errors)
        if(name != null) name = name.trim();

        // Create specification
        Specification<Product> spec = ProductSpecifications.filterBy(
                ProductSpecifications.Filter.builder()
                        .nameLike(name)
                        .typeId(typeId)
                        .sizeIds(sizeIds)
                        .colorIds(colorIds)
                        .minPrice(minPrice)
                        .maxPrice(maxPrice)
                        .build()
        );

        // Create pageable
        Pageable pageable = PageRequest.of(page, size);

        // Get filtered products
        Page<Product> products = productService.findAll(spec, pageable);

        // Convert to DTOs
        Page<ProductCardDto> productDtos = products.map(product -> {
            String imageData = null;
            if (!CollectionUtils.isEmpty(product.getImages())) {
                byte[] firstImageData = product.getImages().getLast().getImageData();
                if (firstImageData != null && firstImageData.length > 0) {
                    imageData = Base64.getEncoder().encodeToString(firstImageData);
                }
            }

            return ProductCardDto.builder()
                    .id(product.getId())
                    .name(product.getName())
                    .description(product.getDescription())
                    .price(product.getPrice())
                    .imageData(imageData)
                    .build();
        });

        return ResponseEntity.ok(productDtos);
    }

    @PreAuthorize("isAuthenticated() and hasRole('ADMIN')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public ResponseEntity<Product> createProduct(
            @RequestPart("name") String name,
            @RequestPart("typeId") String typeId,
            @RequestPart(value = "description", required = false) String description,
            @RequestPart("price") String price,
            @RequestPart(value = "attributes", required = false) String jsonProductAttributes,
            @RequestPart(value = "variants") String jsonProductVariants,
            @RequestPart(value = "images", required = false) Map<String, MultipartFile> images,
            @RequestPart(value = "metadata", required = false) List<ImageMetadataDto> metadata
    ) {
        // Validate price
        if (price == null) return ResponseEntity.badRequest().build();
        BigDecimal transformedPrice = new BigDecimal(price).setScale(2, RoundingMode.FLOOR);
        if(transformedPrice.compareTo(BigDecimal.ZERO) <= 0) return ResponseEntity.badRequest().build();

        // Validate typeId
        if (typeId == null || typeId.isBlank()) return ResponseEntity.badRequest().build();
        long transformedTypeId = 0;
        try {
            transformedTypeId = Long.parseLong(typeId);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
        if (transformedTypeId < 0) return ResponseEntity.badRequest().build();

        // Validate attributes
        ObjectMapper mapper = new ObjectMapper();
        List<String> parsedAttributes = null;
        if (jsonProductAttributes != null && !jsonProductAttributes.isBlank()) {
            try {
                parsedAttributes = mapper.readValue(jsonProductAttributes, new TypeReference<List<String>>() {});
            } catch (JsonProcessingException e) {
                logger.log(Level.WARNING, "Invalid JSON format for attributes: " + jsonProductAttributes, e);
                return ResponseEntity.badRequest().build();
            }
        }

        // Validate name
        if (name == null || name.isBlank()) return ResponseEntity.badRequest().build();
        name = name.trim(); // Trim name (forgive errors)

        // Validate product variants
        if (jsonProductVariants == null || jsonProductVariants.isBlank()) return ResponseEntity.badRequest().build();

        // Check if images are of supported types
        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images.values()) {
                if (!isSupportedImageType(image)) {
                    return ResponseEntity.badRequest().build();
                }
            }
        }

        // Create product
        try {
            Product savedProduct = productService.createProduct(name, transformedTypeId, description, transformedPrice, parsedAttributes, jsonProductVariants,images, metadata);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
        } catch (IllegalArgumentException | NoSuchElementException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Supported image MIME types and extensions for webp conversion
    private static final String[] SUPPORTED_IMAGE_TYPES = {
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/bmp",
            "image/tiff"
    };
    private static final String[] SUPPORTED_EXTENSIONS = {
            ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".tiff", ".tif"
    };

    private boolean isSupportedImageType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null && Arrays.asList(SUPPORTED_IMAGE_TYPES).contains(contentType)) {
            return true;
        }
        String name = file.getOriginalFilename();
        if (name != null) {
            String lower = name.toLowerCase();
            for (String ext : SUPPORTED_EXTENSIONS) {
                if (lower.endsWith(ext)) {
                    return true;
                }
            }
        }
        return false;
    }

    @PreAuthorize("isAuthenticated() and hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        if(id == null || id < 0) return ResponseEntity.badRequest().build();
        try {
            productService.deleteProduct(id);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("isAuthenticated() and hasRole('ADMIN')")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable Long id,
            @RequestPart("name") String name,
            @RequestPart("typeId") String typeId,
            @RequestPart(value = "description", required = false) String description,
            @RequestPart("price") String price,
            @RequestPart(value = "attributes", required = false) String jsonProductAttributes,
            @RequestPart(value = "variants") String jsonProductVariants,

            //image update
            @RequestPart(value = "newImages", required = false) Map<String, MultipartFile> newImages,
            @RequestPart(value = "newImageMetadata", required = false) String jsonNewImagesMetadata,
            @RequestPart(value = "imageColorChanges", required = false) String jsonImageColorChanges,
            @RequestPart(value = "deleteImages", required = false) Long[] deleteImageIds
    ) {
        List<Long> deleteImagesIdsList = deleteImageIds != null ? Arrays.asList(deleteImageIds) : Collections.emptyList();

        try {
            ProductDto updated = productService.updateProductMultipart(
                id, 
                name, 
                typeId, 
                description, 
                price, 
                jsonProductAttributes, 
                jsonProductVariants,

                // image update
                newImages,
                jsonNewImagesMetadata,
                jsonImageColorChanges,
                deleteImagesIdsList
            );
            
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
