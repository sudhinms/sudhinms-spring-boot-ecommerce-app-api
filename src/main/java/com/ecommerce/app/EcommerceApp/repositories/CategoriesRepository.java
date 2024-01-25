package com.ecommerce.app.EcommerceApp.repositories;

import com.ecommerce.app.EcommerceApp.entities.Categories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriesRepository extends JpaRepository<Categories,Long> {
    Categories findByName(String categoryName);
//    @Query(value = "SELECT name, id FROM categories", nativeQuery = true)
//    Categories findCategory(String name);
//    @Query(value = "")

}
