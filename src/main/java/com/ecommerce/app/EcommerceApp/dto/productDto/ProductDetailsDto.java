package com.ecommerce.app.EcommerceApp.dto.productDto;

import com.ecommerce.app.EcommerceApp.entities.Categories;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProductDetailsDto {
    @Nullable
    private Long id;
    private String name;
    private double price;
    private int quantity;
    @Nullable
    private byte[] ProductImage;
    private String category;
}
