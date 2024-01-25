package com.ecommerce.app.EcommerceApp.controllers;

import com.ecommerce.app.EcommerceApp.configuration.JwtService;
import com.ecommerce.app.EcommerceApp.dto.paymentsDto.PaymentDto;
import com.ecommerce.app.EcommerceApp.exceptions.FileReadWriteException;
import com.ecommerce.app.EcommerceApp.services.CartService;
import com.ecommerce.app.EcommerceApp.services.InvoiceGeneratorService;
import com.ecommerce.app.EcommerceApp.services.OrderService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/app/product")
public class CustomerController {

    @Autowired
    private CartService cartService;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private OrderService orderService;

    @Autowired
    private InvoiceGeneratorService invoiceGeneratorService;

    @PostMapping("/cart/add/{id}")
    public ResponseEntity<?> addProductToCart(@PathVariable("id") long id,@RequestHeader (name="Authorization") String token){
        String currentUserEmail=jwtService.extractUsernameFromToken(token.substring(7));
        return cartService.addToCart(id,currentUserEmail);
    }
    @GetMapping("/cart/getAll")
    public ResponseEntity<?> getAllProductsFromCart(@RequestHeader (name="Authorization") String token){
        String currentUserEmail=jwtService.extractUsernameFromToken(token.substring(7));
        return cartService.getAllItemsInCart(currentUserEmail);
    }

    @DeleteMapping("/cart/delete/{id}")
    public ResponseEntity<EntityModel<String>> deleteOneFromCart(@PathVariable("id") long id, @RequestHeader (name="Authorization") String token){
        String currentUserEmail=jwtService.extractUsernameFromToken(token.substring(7));
        return cartService.deleteFromCart(id,currentUserEmail);
    }
    @GetMapping("/order/getAll")
    public ResponseEntity<?> getAllOrders(@RequestHeader (name="Authorization") String token){
        String currentUserEmail=jwtService.extractUsernameFromToken(token.substring(7));
        return orderService.getAllOrdersOfUser(currentUserEmail);
    }
    @PostMapping("/order/create-order/{productId}/{quantity}")
    public ResponseEntity<?> createOrder(@PathVariable("productId") long productId,
                                         @PathVariable("quantity") int quantity,
                                         @RequestHeader (name="Authorization") String token){
        String currentUserEmail=jwtService.extractUsernameFromToken(token.substring(7));
        return orderService.initiateOrdering(productId,currentUserEmail,quantity);
    }
    @PostMapping("/order/confirm-address/{addressId}")
    public ResponseEntity<?> confirmAddress(@RequestHeader (name="Authorization") String token,
                                            @PathVariable("addressId") long addressId){
        return orderService.confirmAddress(addressId);
    }
    @PostMapping("/order/payment/upi/{productId}/{quantity}")
    public ResponseEntity<?> initiateUpiPayment(@RequestBody PaymentDto paymentDto,
                                   @PathVariable("productId") long productId,
                                   @PathVariable("quantity") int quantity,
                                   @RequestHeader (name="Authorization") String token){
        String currentUserEmail=jwtService.extractUsernameFromToken(token.substring(7));
        return orderService.upiPayment(paymentDto,currentUserEmail,productId,quantity);
    }

    @PostMapping("/order/payment/cash-on-delivery")
    public ResponseEntity<?> initiateCashOnDelivery(@RequestHeader (name="Authorization") String token){
        String currentUserEmail=jwtService.extractUsernameFromToken(token.substring(7));
        return orderService.cashOnDeliveryPayment(currentUserEmail);
    }

    @GetMapping("/download-invoice/{id}")
    public ResponseEntity<byte[]>downloadInvoice(@RequestHeader (name="Authorization") String token,@PathVariable long id){
        String email=jwtService.extractUsernameFromToken(token.substring(7));
        byte[]invoiceContext=invoiceGeneratorService.generateInvoice(email,id);
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_PDF);
        httpHeaders.setContentDispositionFormData("attachment","invoice.pdf");
        return new ResponseEntity<>(invoiceContext,httpHeaders, HttpStatus.OK);
    }

    @PatchMapping("/order/return-order/{orderId}")
    public ResponseEntity<?> returnRequest(@PathVariable("orderId") long orderId,
                                           @RequestHeader (name="Authorization") String token) {
        String email = jwtService.extractUsernameFromToken(token.substring(7));
        return orderService.returnProduct(orderId,email);
    }

    @PatchMapping("/order/cancel-order/{orderId}")
    public ResponseEntity<?> cancelOrder(@PathVariable("orderId") long orderId,
                                         @RequestHeader (name="Authorization") String token){
        String email=jwtService.extractUsernameFromToken(token.substring(7));
        return orderService.cancelOrder(orderId,email);
    }



}
