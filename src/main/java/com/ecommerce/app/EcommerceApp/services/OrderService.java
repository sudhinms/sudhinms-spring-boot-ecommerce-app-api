package com.ecommerce.app.EcommerceApp.services;

import com.ecommerce.app.EcommerceApp.controllers.AdminController;
import com.ecommerce.app.EcommerceApp.controllers.CustomerController;
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
import java.util.*;


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
        HttpStatus httpStatus=HttpStatus.OK;
        if(productDetails.getQuantity()<quantity){
            throw new ProductOutOfStockException("Couldn't place order. Only "+productDetails.getQuantity()+" left!!!");
        }
        if(!addresses.isEmpty()){
            collectionModel.add(Link.of("http://localhost:8081/app/product/order/confirm-address/{addressId}"));
        }
        else {
            collectionModel.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(HomeController.class)
                    .createAddress(null, null)).withRel("Create_New_Address"));
            httpStatus=HttpStatus.CONTINUE;
        }
        this.userOrderInfoMap.put("productId",productId);
        this.userOrderInfoMap.put("quantity", (long) quantity);
        return new ResponseEntity<>(collectionModel,httpStatus);
    }

    public ResponseEntity<?> confirmAddress(long addressId){
        Address address=addressRepository.findById(addressId)
                .orElseThrow(()->new AddressNotFoundException("You don't have a valid address..."));
        if(address!=null){
            this.userOrderInfoMap.put("addressId",addressId);
        }
        return choosePaymentMethod();
    }

    public ResponseEntity<EntityModel<String>> choosePaymentMethod(){
        Link link1=Link.of("http://localhost:8081/app/product/order/payment/upi/{productId}/{quantity}");
        Link link2=Link.of("http://localhost:8081/app/product/order/payment/cash-on-delivery/{productId}/{quantity}");
        EntityModel<String> entityModel=EntityModel.of("Choose payment link");
        entityModel.add(link1.withRel("UPI_Payment"));
        entityModel.add(link2.withRel("Cash_On_Delivery"));
        return new ResponseEntity<>(entityModel,HttpStatus.OK);
    }

    public ResponseEntity<?> upiPayment(PaymentDto paymentDto,String email,long productId,int quantity){
        this.paymentService=new UpiPayment();
        userOrderInfoMap.put("productId",productId);
        userOrderInfoMap.put("quantity", (long) quantity);
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
        orders.setUserInfo(userRepository.findByEmail(email).get());
        orders.setExpectedDeliveryDate(Date.from((orders.getOrderDateTime().plusDays(7))
                .atZone(ZoneId.systemDefault()).toInstant()));
        orders.setPaymentStatus(paymentStatus);
        orders.setStatus(OrderStatus.ORDER_PLACED.name());
        orders.setProductDetails(productDetails);
        double total=userOrderInfoMap.get("quantity")*orders.getProductDetails().getPrice();
        orders.setTotalPrice(total);
        Orders savedOrder=orderRepository.save(orders);
        productDetails.setQuantity((productDetails.getQuantity())-(orders.getQuantity()));
        productRepository.save(productDetails);
        return getAllOrdersOfUser(email);
    }


    public ResponseEntity<CollectionModel<Orders>> getAllOrdersOfUser(String email) {
        UserInfo userInfo=userRepository.findByEmail(email)
                .orElseThrow(()->new UsernameNotFoundException("Users not found with username : "+email));
        List<Orders> ordersList=orderRepository.findByUserInfoId(userInfo.getId());
        if(ordersList.isEmpty()){
            throw new InvalidOrderDetailsException("no order found for user : "+email);
        }
        CollectionModel<Orders> collectionModel=CollectionModel.of(ordersList);
        Link link=Link.of("http://localhost:8081/app/product/order/{orderId}");
        collectionModel.add(link.withRel("Single_order_details"));
        return new ResponseEntity<>(collectionModel,HttpStatus.OK);
    }

    public ResponseEntity<?> getAllUserOrder() {
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
        assert orders != null;
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
        UserInfo userInfo=userRepository.findById(order.getUserInfo().getId())
                .orElseThrow(()->new UsernameNotFoundException("No user found"));
        UserDetailsAdminView userDetailsAdminView=new UserDetailsAdminView();
        userDetailsAdminView.setEmail(userInfo.getEmail());
        userDetailsAdminView.setName(userInfo.getName());
        userDetailsAdminView.setMobile(userInfo.getMobile());
        if(userInfo.getProfileImage()!=null){
            userDetailsAdminView.setProfileImage(userInfo.getProfileImage());
        }
        orderDetailDto.setUserDetails(userDetailsAdminView);
        Optional<ProductDetails> productDetails=productRepository.findById(order.getProductDetails().getId());
        productDetails.ifPresent(orderDetailDto::setProductDetails);

        return new ResponseEntity<>(orderDetailDto,HttpStatus.OK);
    }

    public ResponseEntity<?> returnProduct(long orderId, String email) {
        Orders order=orderRepository.findById(orderId)
                .orElseThrow(()->
                        new InvalidOrderDetailsException("Order with order id : "+orderId+" not found"));
        if(!Objects.equals(order.getUserInfo().getEmail(), email)){
            throw new InvalidOrderDetailsException("user '"+email+"' have no order with order id "+orderId);
        }
        String message=null;
        HttpStatus httpStatus=HttpStatus.BAD_REQUEST;
        switch (OrderStatus.valueOf(order.getStatus())){
            case RETURNED:
                throw new ProductDeliveryException("Can't initiate return request because product is already returned");
            case RETURN_REQUEST:
                throw new ProductDeliveryException("Can't initiate return request because product is already requested to return");
            case DELIVERED:
                order.setStatus(OrderStatus.RETURN_REQUEST.name());
                orderRepository.save(order);
                message="Return request initiated successfully";
                httpStatus=HttpStatus.OK;
                break;
            default:
                message="Order is not in a state for return!!!";
                break;
        }
        Link link=WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CustomerController.class)
                .getAllOrders("")).withRel("All_Orders");
        EntityModel<String> entityModel=EntityModel.of(message);
        entityModel.add(link);
        return  new ResponseEntity<>(entityModel,httpStatus);
    }

    public ResponseEntity<EntityModel<String>> cancelOrder(long orderId, String email){
        Orders order=orderRepository.findById(orderId)
                .orElseThrow(()->
                        new InvalidOrderDetailsException("Order with order id : "+orderId+" not found"));
        if(!Objects.equals(order.getUserInfo().getEmail(), email)){
            throw new InvalidOrderDetailsException("user '"+email+"' have no order with order id "+orderId);
        }
        String message=null;
        Link link=WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CustomerController.class)
                .getAllOrders("")).withRel("All_Orders");
        HttpStatus httpStatus=HttpStatus.BAD_REQUEST;
        Link.of("http://localhost:8081/app/product/order/return-order/{orderId}");

        switch (OrderStatus.valueOf(order.getStatus())){

            case DELIVERED:
                message="Can't cancel order because product is delivered.You can request to return product";
                link=Link.of("http://localhost:8081/app/product/order/return-order/{orderId}");
                break;
            case OUT_FOR_DELIVERY:
                message="Can't cancel order because product is out for delivered.You can request to return product when you get it";
                link=Link.of("http://localhost:8081/app/product/order/return-order/{orderId}");
                break;
            case CANCELLED:
                message="Can't cancel order because product is already cancelled";
                break;
            case RETURN_REQUEST:
                throw new ProductDeliveryException("Can't initiate cancel request because product is in return state");
            case RETURNED:
                throw new ProductDeliveryException("Can't initiate cancel request because product is already returned");
            default:
                message="Cancel request initiated successfully";
                order.setStatus(OrderStatus.CANCELLED.name());
                orderRepository.save(order);
                httpStatus=HttpStatus.OK;
                break;
        }
        return new ResponseEntity<>(EntityModel.of(message).add(link),httpStatus);
    }

}
