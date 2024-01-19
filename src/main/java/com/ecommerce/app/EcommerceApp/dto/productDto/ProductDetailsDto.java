package com.ecommerce.app.EcommerceApp.dto.productDto;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProductDetailsDto {
    @Nullable
    private Long id;
    private String name;
    private double price;
    private int quantity;
    private byte[] image;
}
