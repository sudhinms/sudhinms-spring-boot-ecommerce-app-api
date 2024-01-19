package com.ecommerce.app.EcommerceApp.entities;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Builder
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"email"})})
public class UserInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @NotNull
    @Email(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}",message = "Email format is incorrect...")
    private String email;
    @Size(min = 2,max = 100)
    @Pattern(regexp = "^[a-zA-Z]+$",message = "Give valid name")
    private String name;
    @Size(min = 10,max = 270)
    private String password;
    private String role;
    @Pattern(regexp="(^$|[0-9]{10})",message = "Invalid mobile number")
    private String mobile;
    @Nullable
    @Lob
    @Column(name = "profile_image",length = 3145728)
    private byte[] profileImage;
    @OneToMany(mappedBy = "userInfo")
    private List<Address> addresses;
}
