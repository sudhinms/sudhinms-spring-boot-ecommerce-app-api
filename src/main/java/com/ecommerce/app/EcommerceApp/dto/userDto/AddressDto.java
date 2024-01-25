package com.ecommerce.app.EcommerceApp.dto.userDto;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AddressDto {
    @NotBlank(message = "Street name cannot be blank")
    private String street;
    @NotBlank(message = "City name cannot be blank")
    private String city;
    @NotBlank(message = "State name cannot be blank")
    private String state;
    @NotBlank(message = "Pin code cannot be blank")
    private String pin;
}
