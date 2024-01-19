package com.ecommerce.app.EcommerceApp.repositories;

import com.ecommerce.app.EcommerceApp.entities.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Orders,Long> {
}
