package com.ecommerce.app.EcommerceApp.services;

import com.ecommerce.app.EcommerceApp.entities.ProductDetails;
import com.ecommerce.app.EcommerceApp.exceptions.ProductNotFoundException;
import com.ecommerce.app.EcommerceApp.exceptions.ProductOutOfStockException;
import com.ecommerce.app.EcommerceApp.repositories.OrderRepository;
import com.ecommerce.app.EcommerceApp.repositories.ProductRepository;
import com.ecommerce.app.EcommerceApp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class OrderService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderRepository orderRepository;

    private long getUserIdWithEmail(String email){
        return userRepository.findByEmail(email).get().getId();
    }

    public void orderProduct(long id,String email,int quantity){
        ProductDetails productDetails=productRepository.findById(id)
                .orElseThrow(()->new ProductNotFoundException("Product with id : "+id+" not found"));
        if(productDetails.getQuantity()<=0){
            throw new ProductOutOfStockException("product is out of stock..");
        }

    }
}
