package com.ecommerce.app.EcommerceApp.services;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Service
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

    public ResponseEntity<?> addToCart(long id,String email){
        Optional<ProductDetails> optionalProduct=productRepository.findById(id);
        if(optionalProduct.get()==null){
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
        String productImagePath=optionalProduct.get().getImagePath();
        if(Files.exists(Path.of(productImagePath))){
            try {
                cartDto.setImage(Files.readAllBytes(Path.of(product.getImagePath())));
            } catch (IOException e) {
                throw new FileReadWriteException(e.getMessage()+"\nCan't read image from : "+product.getImagePath());
            }
        }
        return new ResponseEntity<>(cartDetails, HttpStatus.OK);
    }

    public ResponseEntity<List<CartDto>> getAllItemsInCart(String email){
        List<CartDetails> cartDetailsList=cartDetailsRepository.findAllByUserId(getUserIdWithEmail(email));
        List<CartDto> allItems=cartDetailsList.stream().map(CartDetails::getProductId).toList()
                .stream().map(productId->productRepository.findById(productId))
                .map(productDetails -> {
                    ProductDetails details=productDetails.get();
                    byte[] image=null;
                    if(Files.exists(Path.of(details.getImagePath()))){
                        try {
                            image=Files.readAllBytes(Path.of(details.getImagePath()));
                        } catch (IOException e) {
                            throw new FileReadWriteException(e.getMessage()+"\nCan't read image from : "+details.getImagePath());
                        }
                    }
                    return new CartDto(details.getId(),details.getName(),details.getPrice(),image);
                }).toList();
        return new ResponseEntity<>(allItems,HttpStatus.OK);
    }

    public ResponseEntity<String> deleteFromCart(long id,String email){
        List<CartDetails> allItemsOfUser=cartDetailsRepository.findAllByUserId(getUserIdWithEmail(email));
        if(allItemsOfUser.stream().filter(product->product.getProductId()==id).toList().isEmpty()){
            throw new ProductNotFoundException("Requested product with id : "+id+" not found");
        }
        cartDetailsRepository.deleteByProductIdAndUserId(id,getUserIdWithEmail(email));
        return new ResponseEntity<>("hh",HttpStatus.OK);
    }
}
