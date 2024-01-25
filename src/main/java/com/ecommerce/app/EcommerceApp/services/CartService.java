package com.ecommerce.app.EcommerceApp.services;

import com.ecommerce.app.EcommerceApp.controllers.CustomerController;
import com.ecommerce.app.EcommerceApp.dto.productDto.CartDto;
import com.ecommerce.app.EcommerceApp.dto.productDto.ProductDetailsDto;
import com.ecommerce.app.EcommerceApp.dto.productDto.ProductDetailsUserView;
import com.ecommerce.app.EcommerceApp.entities.CartDetails;
import com.ecommerce.app.EcommerceApp.entities.ProductDetails;
import com.ecommerce.app.EcommerceApp.entities.UserInfo;
import com.ecommerce.app.EcommerceApp.exceptions.FileReadWriteException;
import com.ecommerce.app.EcommerceApp.exceptions.ProductNotFoundException;
import com.ecommerce.app.EcommerceApp.repositories.CartDetailsRepository;
import com.ecommerce.app.EcommerceApp.repositories.ProductRepository;
import com.ecommerce.app.EcommerceApp.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class CartService {

    @Autowired
    private ProductService productService;
    @Autowired
    private CartDetailsRepository cartDetailsRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;

    private long getUserIdWithEmail(String email){
        return userRepository.findByEmail(email).get().getId();
    }
    private byte[] getImage(String imagePath){
        if(Files.exists(Path.of(imagePath))){
            try {
                return Files.readAllBytes(Path.of(imagePath));
            } catch (IOException e) {
                throw new FileReadWriteException(e.getMessage()+"\nCan't read image from : "+imagePath);
            }
        }
        return new byte[]{};
    }

    public ResponseEntity<?> addToCart(long id,String email){
        Optional<ProductDetails> optionalProduct=productRepository.findById(id);
        if(optionalProduct.isEmpty()){
            throw new ProductNotFoundException("Requested product with id : "+id+" not found");
        }
        List<CartDetails> cartDetailsList=cartDetailsRepository.findAllByUserId(getUserIdWithEmail(email));
        List<CartDetails> existingWithSameId=cartDetailsList.stream()
                .filter(cartItem->cartItem.getProductId()==id).toList();
        if(!existingWithSameId.isEmpty()){
            return new ResponseEntity<>("Item already present in cart..",HttpStatus.FOUND);
        }

        ProductDetails product=optionalProduct.get();

        CartDetails cartDetails=new CartDetails();
        cartDetails.setUserId(getUserIdWithEmail(email));
        cartDetails.setProductId(id);
        cartDetailsRepository.save(cartDetails);

        CartDto cartDto=CartDto.builder()
                .productName(product.getName())
                .productId(product.getId())
                .price(product.getPrice())
                .build();
        if(product.getImagePath()!=null){
            cartDto.setImage(getImage(product.getImagePath()));
        }
        return new ResponseEntity<>(cartDetails, HttpStatus.OK);
    }

    public ResponseEntity<List<CartDto>> getAllItemsInCart(String email){
        List<CartDetails> cartDetailsList=cartDetailsRepository.findAllByUserId(getUserIdWithEmail(email));
        List<CartDto> allItems=cartDetailsList.stream().map(CartDetails::getProductId).toList()
                .stream().map(productId->productRepository.findById(productId))
                .map(productDetails -> {
                    ProductDetails details=productDetails.get();
                    CartDto cartDto=new CartDto();
                    cartDto.setProductId(details.getId());
                    cartDto.setPrice(details.getPrice());
                    cartDto.setProductName(details.getName());
                    if(details.getImagePath()!=null){
                        cartDto.setImage(getImage(details.getImagePath()));
                    }
                    return cartDto;
                }).toList();
        return new ResponseEntity<>(allItems,HttpStatus.OK);
    }

    public ResponseEntity<EntityModel<String>> deleteFromCart(long id,String email){
        if(cartDetailsRepository.findByProductIdAndUserId(id,getUserIdWithEmail(email))!=null){
            cartDetailsRepository.deleteByUserIdAndProductId(getUserIdWithEmail(email),id);
            EntityModel<String> entityModel=EntityModel.of("Product deleted successfully");
            entityModel.add(WebMvcLinkBuilder
                    .linkTo(WebMvcLinkBuilder
                            .methodOn(CustomerController.class)
                            .getAllProductsFromCart(null)).withRel("Get_All_Cart_Items"));
            return new ResponseEntity<>(entityModel,HttpStatus.OK);
        }
        else {
            throw new ProductNotFoundException("Product with id : "+id+"  not found");
        }
    }
}
