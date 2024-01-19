package com.ecommerce.app.EcommerceApp.controllers;

import com.ecommerce.app.EcommerceApp.configuration.JwtService;
import com.ecommerce.app.EcommerceApp.dto.productDto.ProductDetailsDto;
import com.ecommerce.app.EcommerceApp.dto.productDto.ProductDetailsUserView;
import com.ecommerce.app.EcommerceApp.dto.userDto.LoginDto;
import com.ecommerce.app.EcommerceApp.dto.userDto.PasswordDto;
import com.ecommerce.app.EcommerceApp.dto.userDto.UpdateProfileDto;
import com.ecommerce.app.EcommerceApp.dto.userDto.UserInfoDto;
import com.ecommerce.app.EcommerceApp.services.ProductService;
import com.ecommerce.app.EcommerceApp.services.UserService;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/app")
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
    public ResponseEntity<?> registerUser(@ModelAttribute @Valid UserInfoDto userInfoDto
                                              , @RequestParam("image")@Nullable MultipartFile image){
        return userService.registerUser(userInfoDto,image);
    }

    @PostMapping("/login")
    public ResponseEntity<?> registration(@RequestBody @Valid LoginDto loginDto){
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
        String email=jwtService.extractUsernameFromToken(token);
        return userService.getProfile(email);
    }

    @PutMapping("/user/update/password")
    public ResponseEntity<?> updatePassword(@RequestBody @Valid PasswordDto passwordDto,
                                            @RequestHeader (name="Authorization") String token){
        String email=jwtService.extractUsernameFromToken(token);
        return userService.updatePassword(email,passwordDto);
    }

    @PatchMapping("/user/update/profile")
    public ResponseEntity<?> updateProfile(@ModelAttribute @Valid UpdateProfileDto updateProfileDto,
                                           @RequestParam("image") MultipartFile image,
                                           @RequestHeader (name="Authorization") String token){
        String email=jwtService.extractUsernameFromToken(token);
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
}
