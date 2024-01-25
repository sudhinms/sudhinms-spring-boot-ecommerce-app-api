package com.ecommerce.app.EcommerceApp.services;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ProductDeliveryException extends RuntimeException {
    public ProductDeliveryException(String string) {
    }
}
