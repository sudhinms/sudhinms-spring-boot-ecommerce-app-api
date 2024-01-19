//package com.ecommerce.app.EcommerceApp.services;
//
//import com.ecommerce.app.EcommerceApp.entities.ProductDetails;
//import com.ecommerce.app.EcommerceApp.exceptions.FileReadWriteException;
//import com.ecommerce.app.EcommerceApp.repositories.ProductRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.File;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.util.UUID;
//
//@Service
//public class ImageValidationAndStoringService {
//    private String IMAGE_DIRECTORY;
//    @Autowired
//    private ProductRepository productRepository;
//
//    public void setIMAGE_DIRECTORY(String IMAGE_DIRECTORY) {
//        this.IMAGE_DIRECTORY = IMAGE_DIRECTORY;
//    }
//
//    public ResponseEntity<?> validateImageAndSaveData(ProductDetails productDetails, MultipartFile image){
//        if(image!=null){
//            if (!image.getContentType().startsWith("image/")) {
//                return ResponseEntity.badRequest().body("Only images are allowed");
//            }
//            String path=IMAGE_DIRECTORY+image.getOriginalFilename();
//            boolean isExist=checkIfExist(image,path);
//            if(Files.exists(Path.of(path)) && isExist){
//                productDetails.setImagePath(path);
//                return new ResponseEntity<>(productRepository.save(productDetails), HttpStatus.CREATED);
//            }
//            try {
//                if(Files.exists(Path.of(path))){
//                    path=renameImage(image,path);
//                }
//                image.transferTo(new File(path));
//                productDetails.setImagePath(path);
//            }catch (Exception e){
//                throw new FileReadWriteException("Cant upload file : "+image.getOriginalFilename());
//            }
//        }
//        return new ResponseEntity<>(productRepository.save(productDetails), HttpStatus.CREATED);
//    }
//
//    private boolean checkIfExist(MultipartFile image,String path) {
//        if(Files.exists(Path.of(IMAGE_DIRECTORY + image.getOriginalFilename()))){
//            try {
//                long mismatch = Files.mismatch(Path.of(path), Path.of(IMAGE_DIRECTORY + image.getOriginalFilename()));
//                if (mismatch==-1){
//                    return true;
//                }
//            }catch (Exception e){
//                throw new RuntimeException();
//            }
//        }
//        return false;
//    }
//
//    private String renameImage(MultipartFile image,String path){
//        String[] imagePathArray= image.getOriginalFilename().split("\\.");
//        String lastName= UUID.randomUUID().toString().substring(0,5);
//        imagePathArray[0]=imagePathArray[0]+lastName;
//        path=IMAGE_DIRECTORY+String.join(".",imagePathArray);
//        return path;
//    }
//}
