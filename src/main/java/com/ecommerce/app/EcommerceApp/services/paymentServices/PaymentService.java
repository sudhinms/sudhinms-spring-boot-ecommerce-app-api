package com.ecommerce.app.EcommerceApp.services.paymentServices;

import com.ecommerce.app.EcommerceApp.dto.paymentsDto.PaymentDto;

import java.util.HashMap;

public interface PaymentService {
    PaymentDto doPayment(HashMap<String,Long> userOrderInfoMap, PaymentDto paymentDto);
}
