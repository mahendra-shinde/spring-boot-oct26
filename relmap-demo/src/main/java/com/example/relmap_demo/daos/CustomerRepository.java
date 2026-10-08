package com.example.relmap_demo.daos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.relmap_demo.models.Customer;

@Repository 
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
}
