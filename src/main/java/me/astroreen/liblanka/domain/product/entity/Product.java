package me.astroreen.liblanka.domain.product.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "type_id")
    private ProductType type;

    private String name;
    private String description;
    private BigDecimal price;
    private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "color_id")
    private ProductColor color;

    @ManyToOne
    @JoinColumn(name = "size_id")
    private ProductSize size;

    @ManyToMany
    @JoinTable(
            name = "product_attributes",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "attribute_id")
    )
    private HashSet<Attribute> attributes;

    @Lob
    private byte[] imageData;
}
