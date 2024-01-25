package com.ecommerce.app.EcommerceApp.services;

import com.ecommerce.app.EcommerceApp.controllers.AdminController;
import com.ecommerce.app.EcommerceApp.dto.productDto.ProductDetailsDto;
import com.ecommerce.app.EcommerceApp.dto.productDto.ProductDetailsUserView;
import com.ecommerce.app.EcommerceApp.entities.Categories;
import com.ecommerce.app.EcommerceApp.entities.ProductDetails;
import com.ecommerce.app.EcommerceApp.exceptions.FileReadWriteException;
import com.ecommerce.app.EcommerceApp.exceptions.ProductNotFoundException;
import com.ecommerce.app.EcommerceApp.repositories.CategoriesRepository;
import com.ecommerce.app.EcommerceApp.repositories.ProductRepository;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
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
@Slf4j
public class ProductService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoriesRepository categoriesRepository;

    private static final String IMAGE_DIRECTORY="D:/Projects/EcommerceApp/src/main/resources/images/products/";

    private ProductDetails validateImageAndSaveData(ProductDetails productDetails, MultipartFile image){
        ProductDetailsDto productDetailsDto=new ProductDetailsDto();
        if(image!=null){
            if (!image.getContentType().startsWith("image/")) {
                throw new FileReadWriteException("Only images are allowed...");
            }
            String path=IMAGE_DIRECTORY+image.getOriginalFilename();
            boolean isExist=checkIfExist(image,path);
            if(Files.exists(Path.of(path)) && isExist){
                productDetails.setImagePath(path);
                return productRepository.save(productDetails);
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
        ProductDetails details=productRepository.save(productDetails);
        productDetailsDto.setCategory(details.getCategory().getName());
        productDetailsDto.setName(details.getName());
        productDetailsDto.setQuantity(details.getQuantity());
        productDetailsDto.setId(details.getId());
        productDetailsDto.setPrice(details.getPrice());
        return productDetails;
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

    public ResponseEntity<?> createNewProduct(ProductDetailsDto productDetailsDto,@Nullable MultipartFile image){
        ProductDetails productDetails=new ProductDetails();
        productDetails.setName(productDetailsDto.getName());
        productDetails.setPrice(productDetailsDto.getPrice());
        productDetails.setQuantity(productDetailsDto.getQuantity());
        Categories category=categoriesRepository.findByName(productDetailsDto.getCategory().toLowerCase());
        if(category==null){
            throw new RuntimeException("No category found with name : "+productDetailsDto.getCategory());
        }
        productDetails.setCategory(category);
        ProductDetails result= validateImageAndSaveData(productDetails,image);
        EntityModel<ProductDetails> entityModel=EntityModel.of(result);
        entityModel.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(AdminController.class)
                .getAllProducts()).withRel("All_Products"));
        return new ResponseEntity<>(entityModel,HttpStatus.CREATED);
    }


    public ResponseEntity<?> deleteByProductId(long id){
        try{
            productRepository.deleteById(id);
        }catch (Exception e){
            throw new ProductNotFoundException("Product with id : "+id+" not found");
        }
        EntityModel<String> entityModel=EntityModel.of("Product deleted");
        entityModel.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(AdminController.class)
                .getAllProducts()).withRel("Get_All_Products"));
        return new ResponseEntity<>(entityModel,HttpStatus.OK);
    }

    public ResponseEntity<ProductDetailsDto> getSingleProductById(long id){
        try {
            ProductDetails productDetails=productRepository.findById(id).get();
            ProductDetailsDto productDetailsDto=new ProductDetailsDto();
            productDetailsDto.setQuantity(productDetails.getQuantity());
            productDetailsDto.setPrice(productDetails.getPrice());
            productDetailsDto.setName(productDetails.getName());
            productDetailsDto.setId(productDetails.getId());
            productDetailsDto.setCategory(productDetails.getCategory().getName());
            if(productDetails.getImagePath()!=null) {
                productDetailsDto.setProductImage(getImage(productDetails.getImagePath()));
            }
            return new ResponseEntity<>(productDetailsDto,HttpStatus.FOUND);
        }catch (Exception e ){
            throw new ProductNotFoundException("Product with id : "+id+" not found");
        }
    }

    public ResponseEntity<List<ProductDetailsDto>> getAllProducts(){
        List<ProductDetailsDto> allProducts=productRepository.findAll().stream()
                .map(product->{
                    ProductDetailsDto productDetailsDto=new ProductDetailsDto();
                    productDetailsDto.setId(product.getId());
                    productDetailsDto.setName(product.getName());
                    productDetailsDto.setPrice(product.getPrice());
                    productDetailsDto.setQuantity(product.getQuantity());
                    productDetailsDto.setCategory(product.getCategory().getName());
                    if(product.getImagePath()!=null){
                        productDetailsDto.setProductImage(getImage(product.getImagePath()));
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
          return new ResponseEntity<>(validateImageAndSaveData(product,image),HttpStatus.OK);
        }catch (Exception e ){
            throw new ProductNotFoundException("Product with id : "+id+" not found");
        }
    }
    public ResponseEntity<ProductDetailsUserView> getSingleProductByIdForUsersView(long id){
        try {
            ProductDetails productDetails=productRepository.findById(id)
                    .orElseThrow(()-> new ProductNotFoundException("Product with id : "+id+" not found"));
            ProductDetailsUserView productView=new ProductDetailsUserView();
                    productView.setId(productDetails.getId());
                    productView.setPrice(productDetails.getPrice());
                    productView.setName(productDetails.getName());
            if(productDetails.getImagePath()!=null){
                productView.setImage(getImage(productDetails.getImagePath()));
            }
            return new ResponseEntity<>(productView,HttpStatus.OK);
        }catch (Exception e ){
            throw new ProductNotFoundException("Product with id : "+id+" not found");
        }
    }
    public ResponseEntity<List<ProductDetailsUserView>> getAllProductsForUsersView(){
        List<ProductDetailsUserView> allProducts=productRepository.findAll().stream()
                .map(product->{
                    ProductDetailsUserView userView=new ProductDetailsUserView();
                    userView.setId(product.getId());
                    userView.setPrice(product.getPrice());
                    userView.setName(product.getName());
                    if(product.getImagePath()!=null){
                        userView.setImage(getImage(product.getImagePath()));
                    }
                    return userView;
                }).toList();

        return new ResponseEntity<>(allProducts,HttpStatus.OK);
    }
}
