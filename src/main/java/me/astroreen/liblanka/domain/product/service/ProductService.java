package me.astroreen.liblanka.domain.product.service;

import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import me.astroreen.liblanka.domain.product.dto.ProductConstructionInfoDto;
import me.astroreen.liblanka.domain.product.entity.Product;
import me.astroreen.liblanka.domain.product.entity.ProductColor;
import me.astroreen.liblanka.domain.product.entity.ProductSize;
import me.astroreen.liblanka.domain.product.entity.ProductType;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductTypeRepository productTypeRepository;
    private final ProductColorRepository productColorRepository;
    private final ProductSizeRepository productSizeRepository;
    private final ProductVariantRepository productVariantRepository;

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
    public void updateProductColors(Long oldColorId, Long newColorId) {
        productColorRepository.findById(newColorId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid replacement color ID"));
        
        // Update product variants that use the old color
        productVariantRepository.updateColorId(oldColorId, newColorId);
    }

    @Transactional
    public void updateProductSizes(Long oldSizeId, Long newSizeId) {
        productSizeRepository.findById(newSizeId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid replacement size ID"));
        
        // Update product variants that use the old size
        productVariantRepository.updateSizeId(oldSizeId, newSizeId);
    }
}
