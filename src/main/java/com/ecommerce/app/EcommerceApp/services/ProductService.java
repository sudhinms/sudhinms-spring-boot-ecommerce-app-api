package com.ecommerce.app.EcommerceApp.services;

import com.ecommerce.app.EcommerceApp.dto.productDto.ProductDetailsDto;
import com.ecommerce.app.EcommerceApp.dto.productDto.ProductDetailsUserView;
import com.ecommerce.app.EcommerceApp.entities.ProductDetails;
import com.ecommerce.app.EcommerceApp.exceptions.FileReadWriteException;
import com.ecommerce.app.EcommerceApp.exceptions.ProductNotFoundException;
import com.ecommerce.app.EcommerceApp.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    private static final String IMAGE_DIRECTORY="D:/Projects/EcommerceApp/src/main/resources/images/products/";

    public ResponseEntity<?> createProduct(ProductDetailsDto productDetailsDto, MultipartFile image){
        ProductDetails productDetails=ProductDetails.builder()
                .name(productDetailsDto.getName())
                .price(productDetailsDto.getPrice())
                .quantity(productDetailsDto.getQuantity())
                .build();
        return validateImageAndSaveData(productDetails,image);
    }
    private ResponseEntity<?> validateImageAndSaveData(ProductDetails productDetails, MultipartFile image){
        if(image!=null){
            if (!image.getContentType().startsWith("image/")) {
                return ResponseEntity.badRequest().body("Only images are allowed");
            }
            String path=IMAGE_DIRECTORY+image.getOriginalFilename();
            boolean isExist=checkIfExist(image,path);
            if(Files.exists(Path.of(path)) && isExist){
                productDetails.setImagePath(path);
                return new ResponseEntity<>(productRepository.save(productDetails), HttpStatus.CREATED);
            }
            try {
                if(Files.exists(Path.of(path))){
                    path=renameImage(image,path);
                }
                image.transferTo(new File(path));
                productDetails.setImagePath(path);
            }catch (Exception e){
                throw new FileReadWriteException("Cant upload file : "+image.getOriginalFilename());
            }
        }
        return new ResponseEntity<>(productRepository.save(productDetails), HttpStatus.CREATED);
    }

    private boolean checkIfExist(MultipartFile image,String path) {
        if(Files.exists(Path.of(IMAGE_DIRECTORY + image.getOriginalFilename()))){
            try {
                long mismatch = Files.mismatch(Path.of(path), Path.of(IMAGE_DIRECTORY + image.getOriginalFilename()));
                if (mismatch==-1){
                    return true;
                }
            }catch (Exception e){
                throw new RuntimeException();
            }
        }
        return false;
    }

    private String renameImage(MultipartFile image,String path){
        String[] imagePathArray= image.getOriginalFilename().split("\\.");
        String lastName= UUID.randomUUID().toString().substring(0,5);
        imagePathArray[0]=imagePathArray[0]+lastName;
        path=IMAGE_DIRECTORY+String.join(".",imagePathArray);
        return path;
    }

    public ResponseEntity<?> deleteByProductId(long id){
        try{
            productRepository.deleteById(id);
        }catch (Exception e){
            throw new ProductNotFoundException("Product with id : "+id+" not found");
        }
        return new ResponseEntity<>("Product deleted",HttpStatus.OK);
    }

    public ResponseEntity<ProductDetailsDto> getSingleProductById(long id){
        try {
            ProductDetails productDetails=productRepository.findById(id).get();
            ProductDetailsDto productDetailsDto=ProductDetailsDto.builder()
                    .quantity(productDetails.getQuantity())
                    .price(productDetails.getPrice())
                    .name(productDetails.getName())
                    .id(productDetails.getId())
                    .build();
            if(Files.exists(Path.of(productDetails.getImagePath()))){
                try {
                    productDetailsDto.setImage(Files.readAllBytes(Path.of(productDetails.getImagePath())));
                } catch (IOException e) {
                    throw new FileReadWriteException(e.getMessage()+"\nCan't read image from : "+productDetails.getImagePath());
                }
            }
            return new ResponseEntity<>(productDetailsDto,HttpStatus.FOUND);
        }catch (Exception e ){
            throw new ProductNotFoundException("Product with id : "+id+" not found");
        }
    }

    public ResponseEntity<List<ProductDetailsDto>> getAllProducts(){
        List<ProductDetailsDto> allProducts=productRepository.findAll().stream()
                .map(product->{
                    ProductDetailsDto productDetailsDto=ProductDetailsDto.builder()
                             .id(product.getId())
                             .name(product.getName())
                             .price(product.getPrice())
                             .quantity(product.getQuantity())
                             .build();
                    if(Files.exists(Path.of(product.getImagePath()))){
                        try {
                            productDetailsDto.setImage(Files.readAllBytes(Path.of(product.getImagePath())));
                        } catch (IOException e) {
                            throw new FileReadWriteException(e.getMessage()+"\nCan't read image from : "+product.getImagePath());
                        }
                    }
                    return productDetailsDto;
                }).toList();
        return new ResponseEntity<>(allProducts,HttpStatus.OK);
    }

    public ResponseEntity<?> updateProductById(long id,ProductDetailsDto productDetailsDto,MultipartFile image){
        try{
            ProductDetails product = productRepository.findById(id).get();
            product.setName(productDetailsDto.getName());
            product.setPrice(productDetailsDto.getPrice());
            product.setQuantity(productDetailsDto.getQuantity());

//            return new ResponseEntity<>(productRepository.save(product),HttpStatus.OK);
            return validateImageAndSaveData(product,image);
        }catch (Exception e ){
            throw new ProductNotFoundException("Product with id : "+id+" not found");
        }
    }
    public ResponseEntity<ProductDetailsUserView> getSingleProductByIdForUsersView(long id){
        try {
            ProductDetails productDetails=productRepository.findById(id)
                    .orElseThrow(()-> new ProductNotFoundException("Product with id : "+id+" not found"));
            ProductDetailsUserView productView=ProductDetailsUserView.builder()
                    .name(productDetails.getName())
                    .price(productDetails.getPrice())
                    .id(productDetails.getId())
                    .build();
            if(Files.exists(Path.of(productDetails.getImagePath()))){
                try {
                    productView.setImage(Files.readAllBytes(Path.of(productDetails.getImagePath())));
                } catch (IOException e) {
                    throw new FileReadWriteException(e.getMessage()+"\nCan't read image from : "+productDetails.getImagePath());
                }
            }
            return new ResponseEntity<>(productView,HttpStatus.FOUND);
        }catch (Exception e ){
            throw new ProductNotFoundException("Product with id : "+id+" not found");
        }
    }
    public ResponseEntity<List<ProductDetailsUserView>> getAllProductsForUsersView(){
        List<ProductDetailsUserView> allProducts=productRepository.findAll().stream()
                .map(product->{
                    byte[] image=null;
                    if(Files.exists(Path.of(product.getImagePath()))){
                        try {
                            image=Files.readAllBytes(Path.of(product.getImagePath()));
                        } catch (IOException e) {
                            throw new FileReadWriteException(e.getMessage()+"\nCan't read image from : "+product.getImagePath());
                        }
                    }
                     return new ProductDetailsUserView(product.getId(),product.getName(), product.getPrice(),image );
                }).toList();

        return new ResponseEntity<>(allProducts,HttpStatus.OK);
    }
}
