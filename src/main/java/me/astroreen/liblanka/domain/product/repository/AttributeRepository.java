package me.astroreen.liblanka.domain.product.repository;

import me.astroreen.liblanka.domain.product.entity.Attribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AttributeRepository extends JpaRepository<Attribute, Long> {
    Optional<Attribute> findByName(String name);
}
