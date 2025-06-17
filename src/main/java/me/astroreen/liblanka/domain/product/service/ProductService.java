package me.astroreen.liblanka.domain.product.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
}
