package me.astroreen.liblanka.domain.product.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "product_variant")
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "color_id", nullable = false)
    private ProductColor color;

    @ManyToOne
    @JoinColumn(name = "size_id", nullable = false)
    private ProductSize size;

    @Column(nullable = false)
    private Integer quantity;
}
