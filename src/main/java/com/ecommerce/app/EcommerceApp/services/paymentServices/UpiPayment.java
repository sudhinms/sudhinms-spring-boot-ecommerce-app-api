package com.ecommerce.app.EcommerceApp.services.paymentServices;

import com.ecommerce.app.EcommerceApp.dto.paymentsDto.PaymentDto;
import com.ecommerce.app.EcommerceApp.entities.ProductDetails;
import com.ecommerce.app.EcommerceApp.exceptions.ProductNotFoundException;
import com.ecommerce.app.EcommerceApp.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;

public class UpiPayment implements PaymentService{

    @Autowired
    ProductRepository productRepository;

    public PaymentDto doPayment(HashMap<String, Long> userOrderInfoMap, PaymentDto paymentDto) {
        long productId=userOrderInfoMap.get("productId");
        long addressId=userOrderInfoMap.get("addressId");
        int quantity= Math.toIntExact(userOrderInfoMap.get("quantity"));
        ProductDetails productDetails=productRepository.findById(productId)
                .orElseThrow(()-> new ProductNotFoundException("Product not found"));
        paymentDto.setAmount(quantity*productDetails.getPrice());
        return paymentDto;
    }

}
