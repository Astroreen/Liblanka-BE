package me.astroreen.liblanka.domain.product.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
public class ProductDto {

    private Long id;
    private String name;
    private Long typeId;
    private String description;
    private BigDecimal price;
    private String variants; //JSON
    private String attributes; //JSON
    private List<MultipartFile> images;
    private String colorIds; //JSON
}
