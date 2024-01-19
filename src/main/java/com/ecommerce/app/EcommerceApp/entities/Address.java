package com.ecommerce.app.EcommerceApp.entities;

import jakarta.persistence.*;
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
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String street;
    @Size(min = 2,max = 100)
    @Pattern(regexp = "^[a-zA-Z]+$",message = "Give valid name")
    private String city;
    @Size(min = 2,max = 100)
    @Pattern(regexp = "^[a-zA-Z]+$",message = "Give valid name")
    private String state;
    @Pattern(regexp = "^\\\\d{6}$",message = "Invalid pin")
    private String pin;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserInfo userInfo;
}
