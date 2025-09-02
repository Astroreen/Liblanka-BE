package me.astroreen.liblanka.domain.product.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import me.astroreen.liblanka.domain.product.dto.ImageMetadataDto;
import me.astroreen.liblanka.domain.product.dto.ProductConstructionInfoDto;
import me.astroreen.liblanka.domain.product.dto.ProductDto;
import me.astroreen.liblanka.domain.product.dto.ProductImageDto;
import me.astroreen.liblanka.domain.product.dto.ProductVariantDto;
import me.astroreen.liblanka.domain.product.entity.Product;
import me.astroreen.liblanka.domain.product.entity.ProductColor;
import me.astroreen.liblanka.domain.product.entity.ProductImage;
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.Collections;
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
    private final Logger logger = Logger.getLogger(getClass().getName());

    public Product findById(@NotNull Long id) throws NoSuchElementException {
        return productRepository.findById(id).orElseThrow(() -> new NoSuchElementException("There was not such product in database with id " + id));
    }

    /**
     * Creates a new {@link Product} with the specified details, including optional images and variants.
     *
     * @param name         the name of the product (must not be null or blank)
     * @param typeId       the ID of the product type (must not be null)
     * @param description  the description of the product (optional, may be null or blank)
     * @param price        the price of the product (must not be null)
     * @param attributes   a list of product attributes (optional, may be null or blank)
     * @param jsonVariants a JSON string representing product variants (optional, may be null)
     * @param images       a map of image names to image files for the product (optional, may be null)
     * @param metadata     a list of metadata for the images (optional, may be null)
     * @return the created and persisted {@link Product} entity
     * @throws IllegalArgumentException if the provided typeId is invalid or if variant parsing fails
     */
    @Transactional
    public Product createProduct(@NotNull @NotBlank String name,
                                 @NotNull Long typeId,
                                 @Nullable @NotBlank String description,
                                 @NotNull BigDecimal price,
                                 @Nullable @NotBlank List<String> attributes,
                                 @Nullable String jsonVariants,
                                 @Nullable Map<String, MultipartFile> images,
                                 @Nullable List<ImageMetadataDto> metadata)
            throws IllegalArgumentException {

        Product product = Product.builder()
                .name(name)
                .type(productTypeRepository.findById(typeId)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid type ID")))
                .description(description)
                .price(price)
                .attributes(attributes) // JSONB
                .build();

        // Add product images, if exists
        if (images != null && !images.isEmpty()) {
            try {
                List<ProductImage> savedImages = uniteNewImagesWithMetadata(product, images, metadata);
                product.setImages(savedImages);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error while processing product images", e);
            }
        }

        // Add product variants, if exists
        if (jsonVariants != null) {
            try {
                product = parseJsonProductVariants(product, jsonVariants, null);
            } catch (IOException | IllegalArgumentException e) {
                logger.log(Level.SEVERE, "Error while processing product variants", e);
            }
        }

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

    /**
     * Retrieves detailed information about a product, including its images, variants, colors, sizes, and type.
     * <p>
     * This method fetches the product by its ID and constructs a {@link ProductDto} containing:
     * <ul>
     *   <li>Basic product information (ID, name, description, price, attributes)</li>
     *   <li>List of product images as {@link ProductImageDto}</li>
     *   <li>List of product variants as {@link ProductVariantDto}</li>
     *   <li>Map of variants grouped by color ID</li>
     *   <li>List of available colors as {@link ProductColor}</li>
     *   <li>List of available sizes as {@link ProductSize}</li>
     *   <li>Product type as {@link ProductType}</li>
     * </ul>
     * 
     * @param id the ID of the product to retrieve
     * @return a {@link ProductDto} containing all details of the specified product
     * @throws EntityNotFoundException if the product with the given ID does not exist
     */
    @Transactional
    public ProductDto getProductDetails(Long id) {
        Product product = findById(id);

        //Create list of image dtos
        List<ProductImageDto> images = product.getImages() != null ? product.getImages().stream()
            .map((me.astroreen.liblanka.domain.product.entity.ProductImage img) -> ProductImageDto.builder()
                .id(img.getId())
                .productId(id)
                .colorId(img.getColor() != null ? img.getColor().getId() : null)
                .imageId(img.getId() != null ? img.getId().toString() : null)
                .build())
            .collect(Collectors.toList()) : Collections.emptyList();

        // Create list of variant dtos
        List<ProductVariantDto> variants = product.getVariants() != null ? product.getVariants().stream()
            .map(v -> ProductVariantDto.builder()
                .colorId(v.getColor().getId())
                .sizeId(v.getSize().getId())
                .quantity(v.getQuantity())
                .build())
            .collect(Collectors.toList()) : Collections.emptyList();
        // Create map of variants by color
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
        // Create list of color dtos
        List<ProductColor> colors = product.getVariants() != null ? product.getVariants().stream()
            .map(v -> v.getColor())
            .distinct()
            .map(c -> ProductColor.builder().id(c.getId()).name(c.getName()).hex(c.getHex()).build())
            .collect(Collectors.toList()) : Collections.emptyList();
        // Create list of size dtos
        List<ProductSize> sizes = product.getVariants() != null ? product.getVariants().stream()
            .map(v -> v.getSize())
            .distinct()
            .map(s -> ProductSize.builder().id(s.getId()).name(s.getName()).build())
            .collect(Collectors.toList()) : Collections.emptyList();
        // Create product type dto
        ProductType type = product.getType() != null ? ProductType.builder().id(product.getType().getId()).name(product.getType().getName()).build() : null;

        // Create product dto and return it
        return ProductDto.builder()
            .id(product.getId())
            .name(product.getName())
            .typeId(product.getType().getId())
            .typeName(product.getType().getName())
            .description(product.getDescription())
            .price(product.getPrice())
            .attributes(product.getAttributes())
            .images(images)
            .variants(variants)
            .variantsByColor(variantsByColor)
            .colors(colors)
            .sizes(sizes)
            .type(type)
            .build();
    }

    /**
     * Updates an existing product with multipart data, including basic fields, attributes, variants, and images.
     * <p>
     * This method allows updating the product's name, type, description, price, attributes, and variants.
     * It also supports adding new images with metadata, changing the color of existing images, and deleting images.
     * All updates are performed within a transactional context.
     * </p>
     *
     * @param id                     The ID of the product to update.
     * @param name                   The new name of the product (nullable).
     * @param typeId                 The ID of the new product type (nullable).
     * @param description            The new description of the product (nullable).
     * @param price                  The new price of the product as a string (nullable).
     * @param jsonProductAttributes  JSON string representing a list of product attributes (nullable).
     * @param jsonProductVariants    JSON string representing a list of product variants (nullable).
     * @param newImagesMap           Map of image keys to new image files to be added (nullable).
     * @param jsonNewImagesMetadata  JSON string representing metadata for new images (nullable).
     * @param jsonImageColorChanges  JSON string mapping image IDs to new color IDs for color updates (nullable).
     * @param deleteImagesIdsList    List of image IDs to be deleted from the product (nullable).
     * @return                       The updated product details as a {@link ProductDto}.
     * @throws Exception             If any error occurs during the update process, such as parsing JSON or entity not found.
     */
    @Transactional
    public ProductDto updateProductMultipart(
        Long id, 
        String name, 
        String typeId, 
        String description, 
        String price, 
        String jsonProductAttributes, 
        String jsonProductVariants, 
        Map<String, MultipartFile> newImagesMap,    // key -> image file
        String jsonNewImagesMetadata,               // key -> image metadata
        String jsonImageColorChanges,               // imageId -> new colorId
        List<Long> deleteImagesIdsList) 
        throws Exception {
            
        Product product = findById(id);
        // Update name
        if (name != null) product.setName(name);
        // Update description
        if (description != null) product.setDescription(description);
        // Update price
        if (price != null) product.setPrice(new java.math.BigDecimal(price));
        // Update type
        if (typeId != null) product.setType(productTypeRepository.findById(Long.parseLong(typeId)).orElseThrow());

        ObjectMapper mapper = new ObjectMapper(); //transform json to java understandable objects to continue verification
        // Update attributes
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

        // Update images' data
        if (newImagesMap != null && jsonNewImagesMetadata != null) {
            // Process new images
            List<ImageMetadataDto> newImageMetadataDtos = mapper.readValue(jsonNewImagesMetadata, new TypeReference<List<ImageMetadataDto>>() {});
            List<ProductImage> newImages = new ArrayList<>(product.getImages());                // Create new list
            newImages.addAll(uniteNewImagesWithMetadata(product, newImagesMap, newImageMetadataDtos));    // Add new images to existing ones
            product.setImages(newImages);                                                       // Save new images
        }
        if (jsonImageColorChanges != null) {
            // Process image color changes
            Map<Long, Long> colorChanges = mapper.readValue(jsonImageColorChanges, new TypeReference<Map<Long, Long>>() {});
            List<ProductImage> imagesToUpdate = new ArrayList<>(product.getImages());
            for (Map.Entry<Long, Long> entry : colorChanges.entrySet()) {
                Long imageId = entry.getKey();
                Long newColorId = entry.getValue();
                // Find the image to update and change its color
                for (ProductImage img : imagesToUpdate) {
                    if (img.getId().equals(imageId)) {
                        img.setColor(productColorRepository.findById(newColorId).orElseThrow());
                        break;
                    }
                }
            }
            product.setImages(imagesToUpdate);
        }
        if (deleteImagesIdsList != null) {
            // Process image deletions
            List<ProductImage> imagesToUpdate = new ArrayList<>(product.getImages());
            imagesToUpdate.removeIf(img -> deleteImagesIdsList.contains(img.getId()));
            product.setImages(imagesToUpdate);
        }

        // Save product
        productRepository.save(product); // The updated details will now be updated/saved to the database because of cascade settings
        return getProductDetails(id);
    }

    /**
     * Parses a JSON string representing product variants, creates corresponding {@link ProductVariant} entities,
     * associates them with the given original product, and persists them.
     *
     * @param originalProduct the original {@link Product} to associate the variants with
     * @param jsonVariants the JSON string containing the product variants data
     * @param mapper an optional {@link ObjectMapper} for JSON parsing; if null, a new instance is created
     * @return the updated {@link Product} with its variants set
     * @throws IOException if an error occurs during JSON parsing
     * @throws IllegalArgumentException if a referenced color or size ID does not exist
     */
    @Transactional
    public Product parseJsonProductVariants(Product originalProduct, String jsonVariants, @Nullable ObjectMapper mapper) throws IOException {
        if (mapper == null) {
            mapper = new ObjectMapper();
        }
        
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

    /**
     * Parses a JSON string representing a list of product variants into a list of {@link ProductVariantDto}.
     * <p>
     * This method uses the provided {@link ObjectMapper} to parse the JSON string. It manually extracts
     * the required fields from each JSON object to ensure that all necessary data is present and valid.
     * If any required field is missing or invalid, an {@link IllegalArgumentException} is thrown.
     * </p>
     *
     * @param objectMapper the {@link ObjectMapper} to use for parsing the JSON
     * @param jsonVariants the JSON string representing the list of product variants
     * @return a list of {@link ProductVariantDto} parsed from the JSON string
     * @throws IOException if an error occurs while reading the JSON string
     * @throws IllegalArgumentException if any required field is missing or invalid in the JSON data
     */
    private @NotNull List<ProductVariantDto> parseJsonProductVariants(@NotNull ObjectMapper objectMapper, String jsonVariants) throws IOException, IllegalArgumentException {
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

    /**
     * Processes a map of image files and their corresponding metadata to create a list of new {@link ProductImage} entities.
     * <p>
     * For each entry in the provided images map, this method finds the matching metadata by key,
     * retrieves the associated color from the repository, and constructs a new {@code ProductImage} object.
     * The image data is read from the {@link MultipartFile}, and the resulting images are associated with the given product.
     * </p>
     *
     * @param product   the {@link Product} to associate the new images with
     * @param imagesMap a map where the key is a string identifier and the value is the image file as a {@link MultipartFile}
     * @param metadata  a list of {@link ImageMetadataDto} containing metadata for each image, including the key and color ID
     * @return a list of newly created {@link ProductImage} objects associated with the product
     * @throws IOException if an error occurs while reading image data from the files
     */
    private List<ProductImage> uniteNewImagesWithMetadata(Product product, Map<String, MultipartFile> imagesMap, List<ImageMetadataDto> metadata)
        throws IOException
    {
        List<ProductImage> newImages = new ArrayList<>();
        // Process each image
        for (Map.Entry<String, MultipartFile> entry : imagesMap.entrySet()) {
            String key = entry.getKey();
            MultipartFile file = entry.getValue();
            // Find corresponding metadata
            ImageMetadataDto meta = metadata.stream()
                    .filter(m -> m.getKey().equals(key))
                    .findFirst()
                    .orElse(null);
            if (meta != null) {
                ProductImage newImage = ProductImage.builder()
                            .imageData(file.getBytes())
                            .product(product)
                            .color(productColorRepository.findById(meta.getColorId()).orElseThrow())
                            .build();

                // Add the new image if it was created successfully
                newImages.add(newImage);
            }
        }
        return newImages;
    }
}
