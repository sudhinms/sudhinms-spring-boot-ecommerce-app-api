package com.ecommerce.app.EcommerceApp.entities;

import com.ecommerce.app.EcommerceApp.enums.OrderStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDate orderDate;
    @Past(message = "Date must be in the past")
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDate expectedDeliveryDate;
    @Min(value = 1)
    private int quantity;
    private long addressId;
    private OrderStatus status;
}
