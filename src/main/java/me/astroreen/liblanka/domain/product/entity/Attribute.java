package me.astroreen.liblanka.domain.product.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Attribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(unique = true)
    private String name;

    @ManyToMany(mappedBy = "attributes")
    private HashSet<Product> products;
}
