package me.astroreen.liblanka.domain.product.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.astroreen.liblanka.domain.product.entity.ProductColor;
import me.astroreen.liblanka.domain.product.repository.ColorRepository;
import me.astroreen.liblanka.domain.product.util.ColorValidator;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ColorService {

    private @NonNull ColorRepository colorRepository;

    public ProductColor addColor(@NonNull String name, @NotNull String hexValue) {
        if (!ColorValidator.isValidHexColor(hexValue)) {
            throw new IllegalArgumentException("Invalid HEX color format: " + hexValue);
        }

        if(name.isBlank()) {
            throw new IllegalArgumentException("The color name must not consist of spaces.");
        }

        ProductColor color = new ProductColor();
        color.setName(name);
        color.setHexValue(hexValue);
        return colorRepository.save(color);
    }
}

