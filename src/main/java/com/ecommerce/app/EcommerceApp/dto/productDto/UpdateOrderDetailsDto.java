package com.ecommerce.app.EcommerceApp.dto.productDto;

import com.ecommerce.app.EcommerceApp.enums.OrderStatus;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateOrderDetailsDto {
    @Nullable
    private String status;
    @Nullable
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private Date expectedDeliveryDate;
}
