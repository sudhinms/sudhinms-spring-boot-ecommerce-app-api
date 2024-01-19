package com.ecommerce.app.EcommerceApp.repositories;

import com.ecommerce.app.EcommerceApp.entities.CartDetails;
import com.ecommerce.app.EcommerceApp.entities.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartDetailsRepository extends JpaRepository<CartDetails,Long> {

    List<CartDetails> findAllByUserId(long userId);

    void deleteByProductIdAndUserId(long id, long userIdWithEmail);
}
