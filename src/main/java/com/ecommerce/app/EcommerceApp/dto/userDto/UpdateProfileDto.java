package com.ecommerce.app.EcommerceApp.dto.userDto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateProfileDto {
    @Nullable
    private String email;
    @Nullable
    private String name;
    @Nullable
    private String mobile;
}
