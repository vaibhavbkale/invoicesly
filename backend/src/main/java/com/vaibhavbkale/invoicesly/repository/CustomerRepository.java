package com.vaibhavbkale.invoicesly.repository;

import com.vaibhavbkale.invoicesly.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
