package com.ecommerce.app.EcommerceApp.entities;

import com.ecommerce.app.EcommerceApp.enums.OrderStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Past;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
@ToString
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private LocalDateTime orderDateTime;
    @Past(message = "Date must be in the past")
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private Date expectedDeliveryDate;
    @Min(value = 1)
    private int quantity;
    private long addressId;
    private String status;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private UserInfo userInfo;
    private ProductDetails productDetails;
    private String paymentStatus;
//    private double productPrice;
    private double totalPrice;
}
