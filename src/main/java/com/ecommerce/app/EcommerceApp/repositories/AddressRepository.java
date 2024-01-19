package com.ecommerce.app.EcommerceApp.repositories;

import com.ecommerce.app.EcommerceApp.entities.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<Address,Long> {
}
