package me.astroreen.liblanka.domain.product.service;

import jakarta.annotation.Nullable;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Min;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.astroreen.liblanka.domain.product.entity.*;
import me.astroreen.liblanka.domain.product.repository.*;
import me.astroreen.liblanka.domain.product.repository.specification.ProductSpecifications;
import me.astroreen.liblanka.domain.product.util.ColorValidator;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class ProductService {
    private @NonNull ProductRepository productRepository;
    private @NonNull ProductTypeRepository typeRepository;
    private @NonNull ColorRepository colorRepository;
    private @NonNull SizeRepository sizeRepository;
    private @NonNull AttributeRepository attributeRepository;
    private final Logger logger = Logger.getLogger(getClass().getName());

    /// PRODUCTS

    /***/
    public List<Product> createProducts(@NotNull List<ProductRequest> productRequests) {
        List<Product> products = productRequests.stream().map(this::createProduct).toList();

        if (products.size() == 1) {
            Product product = products.getFirst();
            if (product == null) return Collections.emptyList();
            return List.of(productRepository.save(product));
        }

        return productRepository.saveAll(products);
    }

    public @Nullable Product getProductById(@NonNull Long id) {
        return productRepository.findById(id).orElse(null);
    }

    public Page<Product> filter(ProductSpecifications.Filter filter, Pageable pageable) {
        return productRepository.findAll(ProductSpecifications.filterBy(filter), pageable);
    }

    /// TYPES

    /***/
    public void createProductTypes(@NonNull List<String> names){
        if(names.size() == 1) {
            typeRepository.save(createProductType(names.getFirst()));
            return;
        }

        Set<ProductType> types = HashSet.newHashSet(names.size());
        names.forEach(name -> types.add(createProductType(name)));
        typeRepository.saveAll(types);
    }

    private ProductType createProductType(@NonNull String name){
        return ProductType.builder().name(name).build();
    }

    public List<ProductType> getAllProductTypes() {
        return typeRepository.findAll();
    }

    @Transactional
    public void deleteProductType(String delete, String replace) {
        Optional<ProductType> toDelete = typeRepository.findByName(delete);
        Optional<ProductType> toReplace = typeRepository.findByName(replace);

        if(toDelete.isEmpty() || toReplace.isEmpty()) {
            logger.warning(() -> "Could not find values (" + delete + ", " + replace + ") in products' type table");
            return;
        }

        productRepository.updateProductTypeValue(toDelete.get(), toReplace.get());
        typeRepository.delete(toDelete.get());
    }

    private Product createProduct(@NonNull ProductRequest request) {

        Product.ProductBuilder productBuilder = Product.builder()
                .type(request.type)
                .name(request.name)
                .description(request.description)
                .price(request.price)
                .quantity(request.quantity)
                .color(request.color);

        if (request.sizeName != null) {
            Optional<ProductSize> size = sizeRepository.findByName(request.sizeName);

            if (size.isEmpty()) {
                throw new NoSuchElementException("Provided size id does not exist in the database: " + request.sizeName);
            }

            productBuilder.size(size.orElse(null));
        }

        return productBuilder.build();
    }

    public record ProductRequest(@NonNull ProductType type, @NonNull String name, @Nullable String description, @NonNull @Min(0) BigDecimal price,
                                 @Min(0) int quantity, @Nullable ProductColor color, @Nullable String sizeName) {
    }

    /// COLORS

    /***/
    public ProductColor addColor(@NonNull String name, @NotNull String hexValue) {
        if (!ColorValidator.isValidHexColor(hexValue)) {
            throw new IllegalArgumentException("Invalid HEX color format: " + hexValue);
        }

        if(name.isBlank()) {
            throw new IllegalArgumentException("The color name must not consist of spaces.");
        }

        ProductColor color = new ProductColor();
        color.setName(name);
        color.setHexValue(hexValue);
        return colorRepository.save(color);
    }

    public List<ProductColor> getAllProductColors() {
        return colorRepository.findAll();
    }

    /// SIZES

    /***/
    public List<ProductSize> getAllProductSizes() {
        return sizeRepository.findAll();
    }

    public void createProductSizes(@NonNull List<String> names){
        if(names.size() == 1) {
            sizeRepository.save(createProductSize(names.getFirst()));
            return;
        }

        Set<ProductSize> sizes = HashSet.newHashSet(names.size());
        names.forEach(name -> sizes.add(createProductSize(name)));
        sizeRepository.saveAll(sizes);
    }

    private ProductSize createProductSize(@NonNull String name){
        return ProductSize.builder().name(name).build();
    }

    @Transactional
    public void deleteProductSize(String delete, String replace) {
        Optional<ProductSize> toDelete = sizeRepository.findByName(delete);
        Optional<ProductSize> toReplace = sizeRepository.findByName(replace);

        if(toDelete.isEmpty() || toReplace.isEmpty()) {
            logger.warning(() -> "Could not find values (" + delete + ", " + replace + ") in products' type table");
            return;
        }

        productRepository.updateProductSizeValue(toDelete.get(), toReplace.get());
        sizeRepository.delete(toDelete.get());
    }

    ///  ATTRIBUTES

    /***/
    public List<Attribute> getAllProductAttributes() {
        return attributeRepository.findAll();
    }
}
