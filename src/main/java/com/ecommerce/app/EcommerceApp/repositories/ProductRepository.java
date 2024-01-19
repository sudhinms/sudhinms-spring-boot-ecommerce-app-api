package com.ecommerce.app.EcommerceApp.repositories;

import com.ecommerce.app.EcommerceApp.entities.ProductDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<ProductDetails,Long> {
}
