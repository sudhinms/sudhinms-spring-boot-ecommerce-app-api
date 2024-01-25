package com.ecommerce.app.EcommerceApp.controllers;

import com.ecommerce.app.EcommerceApp.dto.productDto.OrderDetailDto;
import com.ecommerce.app.EcommerceApp.dto.productDto.ProductDetailsDto;
import com.ecommerce.app.EcommerceApp.dto.productDto.UpdateOrderDetailsDto;
import com.ecommerce.app.EcommerceApp.services.CategoryService;
import com.ecommerce.app.EcommerceApp.services.OrderService;
import com.ecommerce.app.EcommerceApp.services.ProductService;
import jakarta.annotation.Nullable;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/app/product")
@MultipartConfig(maxFileSize = 10)
@Slf4j
public class AdminController {

    @Autowired
    private ProductService productService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private CategoryService categoryService;


    @PostMapping("/category/create")
    public ResponseEntity<Link> createCategory(@RequestParam("category") String category){
        return categoryService.createCategory(category);
    }
    @GetMapping("/category/getAll")
    public ResponseEntity<?> getAllCategories(){
        return categoryService.allCategories();
    }

    @DeleteMapping("/category/delete/{categoryId}")
    public ResponseEntity<?> deleteCategoryById(@PathVariable("categoryId") long categoryId){
       return categoryService.deleteCategory(categoryId);
    }

    @PostMapping("/create-new-product")
    public ResponseEntity<?> createProduct(@ModelAttribute @Valid ProductDetailsDto productDetailsDto,
                                           @RequestParam("image") @Nullable MultipartFile image){
        return productService.createNewProduct(productDetailsDto,image);
    }

    @PatchMapping("/update-product/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable("id") long id
                                            ,@ModelAttribute @Valid ProductDetailsDto productDetailsDto
                                            ,@RequestParam("image") @Nullable MultipartFile image){
        return productService.updateProductById(id,productDetailsDto,image);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable("id") long id){
        return productService.deleteByProductId(id);
    }

    @GetMapping("/admin-view/{id}")
    public ResponseEntity<ProductDetailsDto> getSingleProduct(@PathVariable("id") long id){
        return productService.getSingleProductById(id);
    }

    @GetMapping("/admin-view/all")
    public ResponseEntity<List<ProductDetailsDto>> getAllProducts(){
        return productService.getAllProducts();
    }

    @GetMapping("/admin/view-all-orders")
    public ResponseEntity<?> getAllOrders(){
        return orderService.getAllUsersOrder();
    }

    @PatchMapping("/order/update/{orderId}")
    public ResponseEntity<?> updateOrder(@PathVariable("orderId") long orderId, @RequestBody UpdateOrderDetailsDto orderDetailsDto){
        return orderService.updateOrderDetails(orderId,orderDetailsDto);
    }
    @GetMapping("/order/{orderId}")
    public ResponseEntity<OrderDetailDto> getOrderDetails(@PathVariable("orderId") long orderId){
        return orderService.getSingleOrder(orderId);
    }
}
