package com.example.relmap_demo.daos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.relmap_demo.models.Customer;

@Repository 
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    @Query("from Customer c where c.name = :cname")
    public List<Customer> findByName(@Param("cname") String name);
}
