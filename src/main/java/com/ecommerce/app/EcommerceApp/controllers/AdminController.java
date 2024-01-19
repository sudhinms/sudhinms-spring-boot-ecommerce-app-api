package com.ecommerce.app.EcommerceApp.controllers;

import com.ecommerce.app.EcommerceApp.dto.productDto.ProductDetailsDto;
import com.ecommerce.app.EcommerceApp.entities.ProductDetails;
import com.ecommerce.app.EcommerceApp.services.ProductService;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/app")
@MultipartConfig(maxFileSize = 10)
public class AdminController {

    @Autowired
    private ProductService productService;

    @PostMapping("/product/create")
    public ResponseEntity<?> addNewProduct(@ModelAttribute @Valid ProductDetailsDto productDetailsDto
                                          ,@RequestParam("image")MultipartFile image){
        return productService.createProduct(productDetailsDto,image);
    }

    @PutMapping("/product/update/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable long id
                                            ,@ModelAttribute @Valid ProductDetailsDto productDetailsDto
                                            ,@RequestParam("image")MultipartFile image){
        return productService.updateProductById(id,productDetailsDto,image);
    }

    @DeleteMapping("/product/delete/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable long id){
        return productService.deleteByProductId(id);
    }

    @GetMapping("/product/admin-view/{id}")
    public ResponseEntity<ProductDetailsDto> getSingleProduct(@PathVariable long id){
        return productService.getSingleProductById(id);
    }

    @GetMapping("/product/admin-view/all")
    public ResponseEntity<List<ProductDetailsDto>> getAllProducts(){
        return productService.getAllProducts();
    }
}
