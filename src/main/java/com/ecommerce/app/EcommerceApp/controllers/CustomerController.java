package com.ecommerce.app.EcommerceApp.controllers;

import com.ecommerce.app.EcommerceApp.configuration.JwtService;
import com.ecommerce.app.EcommerceApp.services.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app/product")
public class CustomerController {

    @Autowired
    private CartService cartService;
    @Autowired
    private JwtService jwtService;

    @PostMapping("/cart/add/{id}")
    public ResponseEntity<?> addProductToCart(@PathVariable long id,@RequestHeader (name="Authorization") String token){
        String currentUserEmail=jwtService.extractUsernameFromToken(token);
        return cartService.addToCart(id,currentUserEmail);
    }
    @GetMapping("/cart/getAll")
    public ResponseEntity<?> getAllProductsFromCart(@RequestHeader (name="Authorization") String token){
        String currentUserEmail=jwtService.extractUsernameFromToken(token);
        return cartService.getAllItemsInCart(currentUserEmail);
    }

    @DeleteMapping("/cart/delete/{id}")
    public ResponseEntity<?> deleteOneFromCart(@PathVariable long id,@RequestHeader (name="Authorization") String token){
        String currentUserEmail=jwtService.extractUsernameFromToken(token);
        return cartService.deleteFromCart(id,currentUserEmail);
    }
}
