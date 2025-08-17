package com.vaibhavbkale.invoicesly.repository;

import com.vaibhavbkale.invoicesly.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
