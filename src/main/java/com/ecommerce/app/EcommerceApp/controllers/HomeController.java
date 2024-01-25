package com.ecommerce.app.EcommerceApp.controllers;

import com.ecommerce.app.EcommerceApp.configuration.JwtService;
import com.ecommerce.app.EcommerceApp.dto.productDto.ProductDetailsDto;
import com.ecommerce.app.EcommerceApp.dto.productDto.ProductDetailsUserView;
import com.ecommerce.app.EcommerceApp.dto.userDto.*;
import com.ecommerce.app.EcommerceApp.entities.Address;
import com.ecommerce.app.EcommerceApp.services.ProductService;
import com.ecommerce.app.EcommerceApp.services.UserService;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/app")
@Slf4j
public class HomeController {

    @Autowired
    private UserService userService;
    @Autowired
    private ProductService productService;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@ModelAttribute @Validated UserInfoDto userInfoDto
                                              ,@RequestParam("image")@Nullable MultipartFile image){
        return userService.registerUser(userInfoDto,image);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginDto loginDto){
        Authentication authentication=authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginDto.getEmail(),loginDto.getPassword()));
        if(authentication.isAuthenticated()){
            return new ResponseEntity<>(jwtService.generateToken(loginDto.getEmail()), HttpStatus.CREATED);
        }
        else {
            throw new UsernameNotFoundException("Not an authorized user!!!!!!");
        }
    }

    @GetMapping("/user/profile")
    public ResponseEntity<?> getProfile(@RequestHeader (name="Authorization") String token){
        String email=jwtService.extractUsernameFromToken(token.substring(7));
        return userService.getProfile(email);
    }

    @PutMapping("/user/update/password")
    public ResponseEntity<?> updatePassword(@RequestBody @Valid PasswordDto passwordDto,
                                            @RequestHeader (name="Authorization") String token){
        String email=jwtService.extractUsernameFromToken(token.substring(7));
        return userService.updatePassword(email,passwordDto);
    }

    @PatchMapping("/user/update/profile")
    public ResponseEntity<?> updateProfile(@ModelAttribute @Valid UpdateProfileDto updateProfileDto,
                                           @RequestParam("image") @Nullable MultipartFile image,
                                           @RequestHeader (name="Authorization") String token){
        String email=jwtService.extractUsernameFromToken(token.substring(7));
        return userService.updateProfile(updateProfileDto,email,image);
    }

    @GetMapping("/product/view/{id}")
    public ResponseEntity<ProductDetailsUserView> getSingleProduct(@PathVariable long id){
        return productService.getSingleProductByIdForUsersView(id);
    }

    @GetMapping("/product/view/all")
    public ResponseEntity<List<ProductDetailsUserView>> getAllProducts(){
        return productService.getAllProductsForUsersView();
    }
    @GetMapping("/user/getAll-address")
    public ResponseEntity<List<Address>> getAllAddresses(@RequestHeader (name="Authorization") String token){
        String currentUserEmail=jwtService.extractUsernameFromToken(token.substring(7));
        return userService.getAllAddress(currentUserEmail);
    }
    @PostMapping("/user/create-address")
    public ResponseEntity<?> createAddress(@RequestBody AddressDto addressDto,
                                           @RequestHeader (name="Authorization") String token){
        String currentUserEmail=jwtService.extractUsernameFromToken(token.substring(7));
        return userService.createNewAddress(currentUserEmail,addressDto);
    }
}
