package com.ecommerce.app.EcommerceApp.repositories;

import com.ecommerce.app.EcommerceApp.entities.CartDetails;
import com.ecommerce.app.EcommerceApp.entities.UserInfo;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartDetailsRepository extends JpaRepository<CartDetails,Long> {

    List<CartDetails> findAllByUserId(long userId);

    @Modifying
    @Transactional
    void deleteByUserIdAndProductId(Long userId, Long productId);

    CartDetails findByProductIdAndUserId(long productId, long userId);
}
