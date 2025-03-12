package me.astroreen.liblanka.domain.product.service;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.astroreen.liblanka.domain.product.entity.Product;
import me.astroreen.liblanka.domain.product.entity.ProductColor;
import me.astroreen.liblanka.domain.product.entity.ProductSize;
import me.astroreen.liblanka.domain.product.entity.ProductType;
import me.astroreen.liblanka.domain.product.repository.ProductRepository;
import me.astroreen.liblanka.domain.product.repository.ProductTypeRepository;
import me.astroreen.liblanka.domain.product.repository.SizeRepository;
import me.astroreen.liblanka.domain.product.repository.specification.ProductSpecifications;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductService {
    private @NonNull ProductRepository productRepository;
    private @NonNull ProductTypeRepository productTypeRepository;
    private @NonNull SizeRepository sizeRepository;

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

    public void createProductTypes(@NonNull List<String> names){
        if(names.size() == 1) {
            productTypeRepository.save(createProductType(names.getFirst()));
            return;
        }

        Set<ProductType> types = HashSet.newHashSet(names.size());
        for(String name : names){
            types.add(createProductType(name));
        }
        productTypeRepository.saveAll(types);
    }

    public List<ProductType> getAllProductTypes() {
        return productTypeRepository.findAll();
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

    private ProductType createProductType(@NonNull String name){
        return ProductType.builder().name(name).build();
    }

    public record ProductRequest(@NonNull ProductType type, @NonNull String name, @Nullable String description, @NonNull @Min(0) BigDecimal price,
                                 @Min(0) int quantity, @Nullable ProductColor color, @Nullable String sizeName) {
    }
}
