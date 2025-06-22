package me.astroreen.liblanka.domain.product.entity.specifications;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.UtilityClass;
import me.astroreen.liblanka.domain.product.entity.Product;
import me.astroreen.liblanka.domain.product.entity.ProductVariant;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;

@UtilityClass
public class ProductSpecifications {

    private static final String NAME = "name";
    private static final String TYPE = "type";
    private static final String COLOR = "color";
    private static final String SIZE = "size";
    private static final String PRICE = "price";
    private static final String PRODUCT = "product";
    private static final String ID = "id";

    @Data
    @Builder
    public static class Filter {
        private String nameLike;
        private Long typeId;
        private List<Long> sizeIds;
        private List<Long> colorIds;
        private BigDecimal minPrice;
        private BigDecimal maxPrice;
    }

    public static @NotNull Specification<Product> filterBy(@NotNull Filter filter) {
        return Specification.where(hasNameLike(filter.getNameLike()))
                .and(hasTypeId(filter.getTypeId()))
                .and(hasSizeIds(filter.getSizeIds()))
                .and(hasColorIds(filter.getColorIds()))
                .and(hasPriceRange(filter.getMinPrice(), filter.getMaxPrice()));
    }

    @Contract(pure = true)
    private static @NotNull Specification<Product> hasNameLike(String name) {
        return (root, query, cb) ->
                name == null || name.isBlank() ? cb.conjunction() :
                        cb.like(cb.lower(root.get(NAME)), "%" + name.toLowerCase() + "%");
    }

    @Contract(pure = true)
    private static @NotNull Specification<Product> hasTypeId(Long typeId) {
        return (root, query, cb) ->
                typeId == null ? cb.conjunction() :
                        cb.equal(root.get(TYPE).get(ID), typeId);
    }

    @Contract(pure = true)
    private static @NotNull Specification<Product> hasSizeIds(List<Long> sizeIds) {
        return (root, query, cb) -> {
            if (sizeIds == null || sizeIds.isEmpty()) {
                return cb.conjunction();
            }

            if(query == null) {
                return cb.conjunction();
            }

            var subquery = query.subquery(Long.class);
            var subqueryRoot = subquery.from(ProductVariant.class);
            subquery.select(subqueryRoot.get(PRODUCT).get(ID))
                .where(subqueryRoot.get(SIZE).get(ID).in(sizeIds))
                .groupBy(subqueryRoot.get(PRODUCT).get(ID))
                .having(cb.equal(cb.countDistinct(subqueryRoot.get(SIZE).get(ID)), (long) sizeIds.size()));

            return root.get(ID).in(subquery);
        };
    }

    @Contract(pure = true)
    private static @NotNull Specification<Product> hasColorIds(List<Long> colorIds) {
        return (root, query, cb) -> {
            if (colorIds == null || colorIds.isEmpty()) {
                return cb.conjunction();
            }

            if(query == null) {
                return cb.conjunction();
            }

            var subquery = query.subquery(Long.class);
            var subqueryRoot = subquery.from(ProductVariant.class);
            subquery.select(subqueryRoot.get(PRODUCT).get(ID))
                    .where(subqueryRoot.get(COLOR).get(ID).in(colorIds))
                    .groupBy(subqueryRoot.get(PRODUCT).get(ID))
                    .having(cb.equal(cb.countDistinct(subqueryRoot.get(COLOR).get(ID)), (long) colorIds.size()));

            return root.get(ID).in(subquery);
        };
    }

    @Contract(pure = true)
    private static @NotNull Specification<Product> hasPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, cb) -> {
            if (minPrice == null && maxPrice == null) {
                return cb.conjunction();
            }

            if (minPrice != null && maxPrice != null) {
                return cb.between(root.get(PRICE), minPrice, maxPrice);
            }

            if (minPrice != null) {
                return cb.greaterThanOrEqualTo(root.get(PRICE), minPrice);
            }

            return cb.lessThanOrEqualTo(root.get(PRICE), maxPrice);
        };
    }
} 