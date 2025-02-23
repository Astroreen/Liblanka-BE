package me.astroreen.liblanka.domain.product.service;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.astroreen.liblanka.domain.product.entity.Attribute;
import me.astroreen.liblanka.domain.product.entity.ProductColor;
import me.astroreen.liblanka.domain.product.entity.Product;
import me.astroreen.liblanka.domain.product.entity.ProductSize;
import me.astroreen.liblanka.domain.product.repository.AttributeRepository;
import me.astroreen.liblanka.domain.product.repository.ColorRepository;
import me.astroreen.liblanka.domain.product.repository.ProductRepository;
import me.astroreen.liblanka.domain.product.repository.SizeRepository;
import me.astroreen.liblanka.domain.product.repository.specification.ProductSpecifications;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private @NonNull ProductRepository productRepository;
    private @NonNull ColorRepository colorRepository;
    private @NonNull SizeRepository sizeRepository;
    private @NonNull AttributeRepository attributeRepository;

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

    private Product createProduct(@NonNull ProductRequest request) {

        Product.ProductBuilder productBuilder = Product.builder()
                .name(request.name)
                .description(request.description)
                .price(request.price)
                .quantity(request.quantity);

        if (request.colorId != null) {
            Optional<ProductColor> color = colorRepository.findById(request.colorId);

            if (color.isEmpty()) {
                throw new NoSuchElementException("Provided color id does not exist in the database: " + request.colorId);
            }

            productBuilder.color(color.orElse(null));
        }

        if (request.sizeId != null) {
            Optional<ProductSize> size = sizeRepository.findById(request.sizeId);

            if (size.isEmpty()) {
                throw new NoSuchElementException("Provided size id does not exist in the database: " + request.sizeId);
            }

            productBuilder.size(size.orElse(null));
        }

        if (request.attributeIds != null) {
            HashSet<Attribute> attributes = HashSet.newHashSet(request.attributeIds.size());

            attributes.addAll(request.attributeIds.stream().map(attrId -> attributeRepository.findById(attrId).orElse(null)).collect(Collectors.toSet()));
            attributes.remove(null);

            productBuilder.attributes(attributes);
        }

        return productBuilder.build();
    }

    public record ProductRequest(@NonNull String name, @Nullable String description, @NonNull @Min(0) BigDecimal price,
                                 @Min(0) int quantity, @Nullable Long colorId, @Nullable Long sizeId,
                                 @Nullable Set<Long> attributeIds) {
    }
}
