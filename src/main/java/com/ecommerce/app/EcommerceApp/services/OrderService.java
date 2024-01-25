package com.ecommerce.app.EcommerceApp.services;

import com.ecommerce.app.EcommerceApp.controllers.AdminController;
import com.ecommerce.app.EcommerceApp.controllers.HomeController;
import com.ecommerce.app.EcommerceApp.dto.paymentsDto.PaymentDto;
import com.ecommerce.app.EcommerceApp.dto.productDto.OrderDetailDto;
import com.ecommerce.app.EcommerceApp.dto.productDto.UpdateOrderDetailsDto;
import com.ecommerce.app.EcommerceApp.dto.userDto.UserDetailsAdminView;
import com.ecommerce.app.EcommerceApp.entities.*;
import com.ecommerce.app.EcommerceApp.enums.OrderStatus;
import com.ecommerce.app.EcommerceApp.enums.PaymentStatus;
import com.ecommerce.app.EcommerceApp.exceptions.AddressNotFoundException;
import com.ecommerce.app.EcommerceApp.exceptions.InvalidOrderDetailsException;
import com.ecommerce.app.EcommerceApp.exceptions.ProductNotFoundException;
import com.ecommerce.app.EcommerceApp.exceptions.ProductOutOfStockException;
import com.ecommerce.app.EcommerceApp.repositories.AddressRepository;
import com.ecommerce.app.EcommerceApp.repositories.OrderRepository;
import com.ecommerce.app.EcommerceApp.repositories.ProductRepository;
import com.ecommerce.app.EcommerceApp.repositories.UserRepository;
import com.ecommerce.app.EcommerceApp.services.paymentServices.PaymentService;
import com.ecommerce.app.EcommerceApp.services.paymentServices.UpiPayment;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;


@Service
@Slf4j
public class OrderService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private AddressRepository addressRepository;

    private PaymentService paymentService;
    private final HashMap<String,Long> userOrderInfoMap=new HashMap<>();

    private long getUserIdWithEmail(String email){
        return userRepository.findByEmail(email).get().getId();
    }

    private ProductDetails checkQuantity(long productId){
        ProductDetails productDetails=productRepository.findById(productId)
                .orElseThrow(()->new ProductNotFoundException("Product with id : "+productId+" not found"));
        if(productDetails.getQuantity()<=0){
            throw new ProductOutOfStockException("product is out of stock..");
        }
        return productDetails;
    }

    public ResponseEntity<?> initiateOrdering(long productId,String email,int quantity){
        checkQuantity(productId);
        List<Address> addresses= addressRepository.findByUserInfoId(getUserIdWithEmail(email))
                .orElseThrow(()->new AddressNotFoundException("You don't have a valid address..."));
        CollectionModel<Address> collectionModel=CollectionModel.of(addresses);
        ProductDetails productDetails=checkQuantity(productId);
        if(productDetails.getQuantity()<quantity){
            throw new ProductOutOfStockException("Couldn't place order. Only "+productDetails.getQuantity()+" left!!!");
        }
        if(addresses.size()>=1){
            collectionModel.add(Link.of("http://localhost:8081/app/product/order/confirm-address/{addressId}"));
        }
        else {
            collectionModel.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(HomeController.class)
                    .createAddress(null, null)).withRel("Create_New_Address"));
        }
        this.userOrderInfoMap.put("productId",productId);
        this.userOrderInfoMap.put("quantity", (long) quantity);
        return new ResponseEntity<>(collectionModel,HttpStatus.FOUND);
    }

    public ResponseEntity<?> confirmAddress(long addressId){
        Address address=addressRepository.findById(addressId)
                .orElseThrow(()->new AddressNotFoundException("You don't have a valid address..."));
        if(address!=null){
            this.userOrderInfoMap.put("addressId",addressId);
        }
        return choosePaymentMethod();
    }

    public ResponseEntity<EntityModel<Link>> choosePaymentMethod(){
        Link link1=Link.of("http://localhost:8081/app/product/order/payment/upi/{productId}/{quantity}");
        Link link2=Link.of("http://localhost:8081/app/product/order/payment/cash-on-delivery/{productId}/{quantity}");
        EntityModel<Link> entityModel=EntityModel.of(link1.withRel("UPI_payment"));
        entityModel.add(link2.withRel("Cash_On_Delivery"));
        return new ResponseEntity<>(entityModel,HttpStatus.OK);
    }

    public ResponseEntity<?> upiPayment(PaymentDto paymentDto,String email){
        this.paymentService=new UpiPayment();
        PaymentDto paymentDto1=paymentService.doPayment(userOrderInfoMap,paymentDto);
        return confirmOrder(email,userOrderInfoMap, PaymentStatus.PAYED.name());
    }

    public ResponseEntity<?> cashOnDeliveryPayment(String email){
        return confirmOrder(email,this.userOrderInfoMap,PaymentStatus.CASH_ON_DELIVERY.name());
    }

    private ResponseEntity<?> confirmOrder(String email, HashMap<String,Long> userOrderInfoMap,String paymentStatus) {
        UserInfo userInfo = userRepository.findByEmail(email)
                .orElseThrow(()->new UsernameNotFoundException("User not found with username : "+email));
        Address address=addressRepository.findById(userOrderInfoMap.get("addressId"))
                .orElseThrow(()->new AddressNotFoundException("You don't have a valid address..."));
        if(userInfo.getId()!=address.getUserInfo().getId()){
            throw new AddressNotFoundException("Invalid address id");
        }
        ProductDetails productDetails=productRepository.findById(userOrderInfoMap.get("productId"))
                .orElseThrow(()->new ProductNotFoundException("product not found"));

        Orders orders=new Orders();
        orders.setOrderDateTime(LocalDateTime.now());
        orders.setQuantity(Math.toIntExact(userOrderInfoMap.get("quantity")));
        orders.setAddressId(userOrderInfoMap.get("addressId"));
        orders.setUserId(getUserIdWithEmail(email));
        orders.setProductId(userOrderInfoMap.get("productId"));
        orders.setExpectedDeliveryDate(Date.from((orders.getOrderDateTime().plusDays(7))
                .atZone(ZoneId.systemDefault()).toInstant()));
        orders.setPaymentStatus(paymentStatus);
        orders.setStatus(OrderStatus.ORDER_PLACED.name());
        orders.setProductPrice(productDetails.getPrice());
        double total=userOrderInfoMap.get("quantity")*orders.getProductPrice();
        orders.setTotalPrice(total);
        Orders savedOrder=orderRepository.save(orders);
        return getAllOrdersOfUser(email);
    }


    public ResponseEntity<CollectionModel<Orders>> getAllOrdersOfUser(String email) {
        UserInfo userInfo=userRepository.findByEmail(email)
                .orElseThrow(()->new UsernameNotFoundException("Users not found with username : "+email));
        List<Orders> ordersList=orderRepository.findByUserId(getUserIdWithEmail(email))
                .orElseThrow(()->new InvalidOrderDetailsException("No order found for : "+email));
        CollectionModel<Orders> collectionModel=CollectionModel.of(ordersList);
        Link link=Link.of("http://localhost:8081/app/product/order/{orderId}");
        collectionModel.add(link.withRel("Single_order_details"));
        return new ResponseEntity<>(collectionModel,HttpStatus.OK);
    }

    public ResponseEntity<?> getAllUsersOrder() {
        return new ResponseEntity<>(orderRepository.findAll(),HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<?> updateOrderDetails(long orderId, UpdateOrderDetailsDto orderDetailsDto) {
            Orders orders = orderRepository.findById(orderId)
                    .orElseThrow(() -> new InvalidOrderDetailsException("Order with id : " + orderId + " not found"));
            if (orders != null) {
                if (orderDetailsDto.getExpectedDeliveryDate() != null) {
                    orders.setExpectedDeliveryDate(orderDetailsDto.getExpectedDeliveryDate());
                }
                if (orderDetailsDto.getStatus() != null) {
                    try {
                        orders.setStatus(OrderStatus.valueOf(orderDetailsDto.getStatus()).name());
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Illegal argument for field 'status'");
                    }
                }
                orders = orderRepository.save(orders);
            }
            EntityModel<Orders> entityModel = EntityModel.of(orders);
            entityModel.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(AdminController.class)
                    .getAllOrders()).withRel("All_Orders"));
            return new ResponseEntity<>(entityModel, HttpStatus.ACCEPTED);
    }

    public ResponseEntity<OrderDetailDto> getSingleOrder(long orderId) {
        Orders order=orderRepository.findById(orderId)
                .orElseThrow(()->new InvalidOrderDetailsException("No order found with id : "+orderId));
        if(order==null){
            throw new InvalidOrderDetailsException("Order not found with id : "+orderId);
        }
        OrderDetailDto orderDetailDto=new OrderDetailDto();
        orderDetailDto.setId(order.getId());
        orderDetailDto.setOrderDateTime(order.getOrderDateTime());
        orderDetailDto.setExpectedDeliveryDate(order.getExpectedDeliveryDate());
        orderDetailDto.setQuantity(order.getQuantity());
        Optional<Address> address=addressRepository.findById(order.getAddressId());
        address.ifPresent(orderDetailDto::setAddress);
        orderDetailDto.setStatus(order.getStatus());
        UserInfo userInfo=userRepository.findById(order.getUserId())
                .orElseThrow(()->new UsernameNotFoundException("No user found"));
        UserDetailsAdminView userDetailsAdminView=new UserDetailsAdminView();
        userDetailsAdminView.setEmail(userInfo.getEmail());
        userDetailsAdminView.setName(userInfo.getName());
        userDetailsAdminView.setMobile(userInfo.getMobile());
        if(userInfo.getProfileImage()!=null){
            userDetailsAdminView.setProfileImage(userInfo.getProfileImage());
        }
        orderDetailDto.setUserDetails(userDetailsAdminView);
        Optional<ProductDetails> productDetails=productRepository.findById(order.getProductId());
        productDetails.ifPresent(orderDetailDto::setProductDetails);

        return new ResponseEntity<>(orderDetailDto,HttpStatus.OK);
    }
}
