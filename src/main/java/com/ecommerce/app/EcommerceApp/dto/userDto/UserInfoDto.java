package com.ecommerce.app.EcommerceApp.dto.userDto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserInfoDto {
    @NotBlank
    private String email;
    @Size(min = 2,max = 100)
    @Pattern(regexp = "^[a-zA-Z]+$")
    private String name;
    private String password;
    @Pattern(regexp="(^$|[0-9]{10})",message = "Invalid mobile number")
    @NotBlank
    private String mobile;
    @Nullable
    private byte[] profileImage;
}
