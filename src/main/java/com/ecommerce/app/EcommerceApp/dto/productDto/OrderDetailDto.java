package com.ecommerce.app.EcommerceApp.dto.productDto;

import com.ecommerce.app.EcommerceApp.dto.userDto.UserDetailsAdminView;
import com.ecommerce.app.EcommerceApp.dto.userDto.UserInfoDto;
import com.ecommerce.app.EcommerceApp.entities.Address;
import com.ecommerce.app.EcommerceApp.entities.ProductDetails;
import com.ecommerce.app.EcommerceApp.entities.UserInfo;
import com.ecommerce.app.EcommerceApp.enums.OrderStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class OrderDetailDto {
    private long id;
    private LocalDateTime orderDateTime;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private Date expectedDeliveryDate;
    @NotNull
    private String status;
    private int quantity;
    private ProductDetails productDetails;
    private UserDetailsAdminView UserDetails;
    private Address address;
}
