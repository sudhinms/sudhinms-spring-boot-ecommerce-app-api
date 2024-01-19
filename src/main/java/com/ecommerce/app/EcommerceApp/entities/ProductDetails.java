package com.ecommerce.app.EcommerceApp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
public class ProductDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Size(min = 2,max = 100)
    @Pattern(regexp = "^[a-zA-Z]+$",message = "Give valid product name")
    private String name;
    @DecimalMin("1.0")
    private double price;
    @Min(1)
    private int quantity;
    private String imagePath;
    @ManyToOne
    private Categories category;
}
