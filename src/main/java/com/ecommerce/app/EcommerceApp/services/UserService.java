package com.ecommerce.app.EcommerceApp.services;

import com.ecommerce.app.EcommerceApp.controllers.CustomerController;
import com.ecommerce.app.EcommerceApp.controllers.HomeController;
import com.ecommerce.app.EcommerceApp.dto.userDto.AddressDto;
import com.ecommerce.app.EcommerceApp.dto.userDto.PasswordDto;
import com.ecommerce.app.EcommerceApp.dto.userDto.UpdateProfileDto;
import com.ecommerce.app.EcommerceApp.dto.userDto.UserInfoDto;
import com.ecommerce.app.EcommerceApp.entities.Address;
import com.ecommerce.app.EcommerceApp.entities.UserInfo;
import com.ecommerce.app.EcommerceApp.enums.Role;
import com.ecommerce.app.EcommerceApp.exceptions.AddressNotFoundException;
import com.ecommerce.app.EcommerceApp.exceptions.FileReadWriteException;
import com.ecommerce.app.EcommerceApp.exceptions.PasswordNotMatchException;
import com.ecommerce.app.EcommerceApp.repositories.AddressRepository;
import com.ecommerce.app.EcommerceApp.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.Optional;


@Service
@Slf4j
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AddressRepository addressRepository;

    public ResponseEntity<?> registerUser(UserInfoDto detailsDto, MultipartFile image){
        if(isUserAlreadyPresent(detailsDto.getEmail())){
            return new ResponseEntity<>("User already present. Try login",HttpStatus.ACCEPTED);
        }
        UserInfo userInfo=UserInfo.builder()
                .email(detailsDto.getEmail())
                .name(detailsDto.getName())
                .password(passwordEncoder.encode(detailsDto.getPassword()))
                .mobile(detailsDto.getMobile())
                .role(Role.ROLE_ADMIN.name())
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
        UserInfo userInfo1=userRepository.save(userInfo);
        userInfo1.setPassword("**********");
        return new ResponseEntity<>(userInfo1,HttpStatus.CREATED);
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
        if(!passwordEncoder.matches(passwordDto.getOldPassword(), userInfo.getPassword())){
            throw new PasswordNotMatchException("Incorrect password for user : "+email);
        }
        if(!Objects.equals(passwordDto.getNewPassword(), passwordDto.getConfirmPassword())){
            throw new PasswordNotMatchException("Confirm password not match with new password");
        }
        userInfo.setPassword(passwordEncoder.encode(passwordDto.getNewPassword()));
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

    public ResponseEntity<List<Address>> getAllAddress(String currentUserEmail) {
        UserInfo userInfo=userRepository.findByEmail(currentUserEmail)
                .orElseThrow(()->new UsernameNotFoundException("Invalid username"));
        List<Address> addressList=addressRepository.findByUserInfoId(userInfo.getId())
                .orElseThrow(()->new AddressNotFoundException("You don't have any address associated with your account"));
        if(addressList.isEmpty()){
            throw new AddressNotFoundException("You don't have any address associated with your account");
        }
        return new ResponseEntity<>(addressList,HttpStatus.OK);
    }
    public ResponseEntity<Link> createNewAddress(String email, AddressDto addressDto){
        UserInfo userInfo=userRepository.findByEmail(email)
                .orElseThrow(()->new UsernameNotFoundException("Users not found with username : "+email));
        Address address= new Address();
        address.setCity(address.getCity());
        address.setPin(addressDto.getPin());
        address.setState(addressDto.getState());
        address.setStreet(addressDto.getStreet());
        address.setUserInfo(userInfo);
        addressRepository.save(address);
        Link link= WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(HomeController.class)
                .getAllAddresses(null)).withRel("Get_All_Addresses");
        return new ResponseEntity<>(link,HttpStatus.CREATED);
    }

}
