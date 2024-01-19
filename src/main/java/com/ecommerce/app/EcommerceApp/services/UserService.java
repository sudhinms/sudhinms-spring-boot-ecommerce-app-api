package com.ecommerce.app.EcommerceApp.services;

import com.ecommerce.app.EcommerceApp.dto.userDto.PasswordDto;
import com.ecommerce.app.EcommerceApp.dto.userDto.UpdateProfileDto;
import com.ecommerce.app.EcommerceApp.dto.userDto.UserInfoDto;
import com.ecommerce.app.EcommerceApp.entities.UserInfo;
import com.ecommerce.app.EcommerceApp.enums.Role;
import com.ecommerce.app.EcommerceApp.exceptions.FileReadWriteException;
import com.ecommerce.app.EcommerceApp.exceptions.PasswordNotMatchException;
import com.ecommerce.app.EcommerceApp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;


@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public ResponseEntity<?> registerUser(UserInfoDto detailsDto, MultipartFile image){
        if(isUserAlreadyPresent(detailsDto.getEmail())){
            return new ResponseEntity<>("User already present. Try login",HttpStatus.ACCEPTED);
        }
        UserInfo userInfo=UserInfo.builder()
                .email(detailsDto.getEmail())
                .name(detailsDto.getName())
                .password(passwordEncoder.encode(detailsDto.getPassword()))
                .mobile(detailsDto.getMobile())
                .role(Role.ROLE_USER.name())
                .build();
        return validateAndSetImage(image,userInfo);
    }

    private ResponseEntity<?> validateAndSetImage(MultipartFile image,UserInfo userInfo){
        if(image!=null){
            try {
                if (!image.getContentType().startsWith("image/")) {
                    return ResponseEntity.badRequest().body("Only images are allowed");
                }
                userInfo.setProfileImage(image.getBytes());
            }catch (Exception e){
                throw new FileReadWriteException("Cant write image data to db");
            }
        }
        return new ResponseEntity<>(userRepository.save(userInfo),HttpStatus.CREATED);
    }

    public boolean isUserAlreadyPresent(String email){
        return userRepository.findByEmail(email).isPresent();
    }

    public ResponseEntity<?> getProfile(String email) {
        UserInfo userInfo=userRepository.findByEmail(email).get();
        UserInfoDto userInfoDto = UserInfoDto.builder()
                .email(userInfo.getEmail())
                .name(userInfo.getName())
                .mobile(userInfo.getMobile())
                .password("**********")
                .build();
        if(userInfo.getProfileImage()!=null){
            userInfoDto.setProfileImage(userInfo.getProfileImage());
        }
        return new ResponseEntity<>(userInfoDto,HttpStatus.OK);
    }

    public ResponseEntity<?> updatePassword(String email, PasswordDto passwordDto) {
        UserInfo userInfo=userRepository.findByEmail(email).get();
        if(!userInfo.getPassword().equals(passwordDto.getOldPassword())){
            throw new PasswordNotMatchException("Incorrect password for user : "+email);
        }
        if(!Objects.equals(passwordDto.getNewPassword(), passwordDto.getConfirmPassword())){
            throw new PasswordNotMatchException("Confirm password not match with new password");
        }
        userInfo.setPassword(passwordDto.getNewPassword());
        userRepository.save(userInfo);
        return new ResponseEntity<>("Password updated successfully..",HttpStatus.OK);
    }

    public ResponseEntity<?> updateProfile(UpdateProfileDto updateProfileDto,String email,MultipartFile image) {
        UserInfo userInfo=userRepository.findByEmail(email).get();
        if(updateProfileDto.getName()!=null){
            userInfo.setName(updateProfileDto.getName());
        }
        if(updateProfileDto.getMobile()!=null){
            userInfo.setMobile(updateProfileDto.getMobile());
        }
        if(updateProfileDto.getEmail()!=null){
            userInfo.setEmail(updateProfileDto.getEmail());
        }
       return validateAndSetImage(image,userInfo);
    }
}
