package com.ecommerce.app.EcommerceApp.services;

import com.ecommerce.app.EcommerceApp.entities.Orders;
import com.ecommerce.app.EcommerceApp.entities.ProductDetails;
import com.ecommerce.app.EcommerceApp.entities.UserInfo;
import com.ecommerce.app.EcommerceApp.exceptions.InvalidOrderDetailsException;
import com.ecommerce.app.EcommerceApp.exceptions.ProductNotFoundException;
import com.ecommerce.app.EcommerceApp.repositories.OrderRepository;
import com.ecommerce.app.EcommerceApp.repositories.ProductRepository;
import com.ecommerce.app.EcommerceApp.repositories.UserRepository;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class InvoiceGeneratorService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;

    public byte[] generateInvoice(String email,long id){

        try {

            ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
            Document document=new Document();
            PdfWriter.getInstance(document,outputStream);
            document.open();

            document.add(new Paragraph(generateInvoiceContext(email,id)));

            document.close();

            return outputStream.toByteArray();

        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }

    }

    private String generateInvoiceContext(String email,long orderId){

        Orders order=orderRepository.findById(orderId)
                .orElseThrow(()->new InvalidOrderDetailsException("Order with id : "+orderId+" not found"));

        StringBuilder invoiceContext=new StringBuilder();

        UserInfo userInfo=userRepository.findByEmail(email)
                .orElseThrow(()->new UsernameNotFoundException("No user found with username : "+email));

        ProductDetails productDetails=productRepository.findById(order.getProductId())
                .orElseThrow(()->new ProductNotFoundException("product not found"));

        invoiceContext.append("Invoice for OrderId: "+order.getId()).append("\n");
        invoiceContext.append("User: "+userInfo.getName()).append("\n");
        invoiceContext.append("product:"+productDetails.getName()).append("\n");
        invoiceContext.append("Amount: "+order.getProductPrice()).append("\n");

        return invoiceContext.toString();

    }
}
