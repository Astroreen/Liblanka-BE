package me.astroreen.liblanka.domain.product.repository.specification;

import lombok.experimental.UtilityClass;
import me.astroreen.liblanka.domain.product.entity.Product;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;

@UtilityClass
public class ProductSpecifications {
    private static final String NAME = "name";
    private static final String PRICE = "price";
    private static final String QUANTITY = "quantity";
    private static final String COLOR = "color";
    private static final String SIZE = "size";
    private static final String ATTRIBUTES = "attributes";


    public static @NotNull Specification<Product> filterBy(@NotNull ProductSpecifications.Filter filter) {
        return Specification.where(hasNameLike(filter.nameLike))
                .and(hasPriceGreaterThan(filter.priceFrom))
                .and(hasPriceLessThan(filter.priceTo))
                .and(hasQuantityGreaterThan(filter.quantityFrom))
                .and(hasQuantityLessThan(filter.quantityTo))
                .and(hasColorIdsIn(filter.colorIds))
                .and(hasSizeIdsIn(filter.sizeIds))
                .and(hasAttributeIdsIn(filter.attributesIds));
    }

    @Contract(pure = true)
    private static @NotNull Specification<Product> hasNameLike(String name) {
        return ((root, query, cb) ->
                name == null || name.isBlank() ? cb.conjunction() : cb.like(cb.lower(root.get(NAME)), "%" + name.toLowerCase() + "%"));
    }

    @Contract(pure = true)
    private static @NotNull Specification<Product> hasPriceGreaterThan(BigDecimal from) {
        return ((root, query, cb) ->
                from == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get(PRICE), from.doubleValue()));
    }

    @Contract(pure = true)
    private static @NotNull Specification<Product> hasPriceLessThan(BigDecimal to) {
        return ((root, query, cb) ->
                to == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get(PRICE), to.doubleValue()));
    }

    @Contract(pure = true)
    private static @NotNull Specification<Product> hasQuantityGreaterThan(Integer from) {
        return ((root, query, cb) ->
                from == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get(QUANTITY), from));
    }

    @Contract(pure = true)
    private static @NotNull Specification<Product> hasQuantityLessThan(Integer to) {
        return ((root, query, cb) ->
                to == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get(QUANTITY), to));
    }

    @Contract(pure = true)
    private static @NotNull Specification<Product> hasColorIdsIn(List<Long> colorIds) {
        return ((root, query, cb) ->
                colorIds == null ? cb.conjunction() : root.get(COLOR).in(colorIds));
    }

    @Contract(pure = true)
    private static @NotNull Specification<Product> hasSizeIdsIn(List<Long> sizeIds) {
        return ((root, query, cb) ->
                sizeIds == null ? cb.conjunction() : root.get(SIZE).in(sizeIds));
    }

    @Contract(pure = true)
    private static @NotNull Specification<Product> hasAttributeIdsIn(List<Long> attributeIds) {
        return ((root, query, cb) ->
                attributeIds == null ? cb.conjunction() : root.get(ATTRIBUTES).in(attributeIds));
    }

    public record Filter(
            String nameLike,
            BigDecimal priceFrom,
            BigDecimal priceTo,
            Integer quantityFrom,
            Integer quantityTo,
            List<Long> colorIds,
            List<Long> sizeIds,
            List<Long> attributesIds) {
    }
}
