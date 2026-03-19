package com.project.revconnect.repository;

import com.project.revconnect.model.Product;
import com.project.revconnect.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByUser(User user);
}