package me.astroreen.liblanka.domain.product.entity;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "product_images")
@ToString(exclude = {"imageData"}) // prevent flood in my console!
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    @JsonBackReference
    private Product product;

    @Lob
    @Column(name = "image_data", nullable = false, columnDefinition = "blob")
    private byte[] imageData;

    @ManyToOne
    @JoinColumn(name = "color_id", nullable = true)
    @Nullable
    private ProductColor color; // can be null
}
