package com.ecomm.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecomm.model.Product;

public interface ProductRepository extends JpaRepository<Product, Integer>
{

}
