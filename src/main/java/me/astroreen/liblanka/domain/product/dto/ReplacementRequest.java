package me.astroreen.liblanka.domain.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReplacementRequest<T> {
    private T itemToDelete;
    private T replacementItem;
} 