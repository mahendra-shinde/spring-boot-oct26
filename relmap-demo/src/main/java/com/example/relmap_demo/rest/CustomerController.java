package com.example.relmap_demo.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.web.bind.annotation.*;

import com.example.relmap_demo.daos.CustomerRepository;
import com.example.relmap_demo.models.Customer;

@RestController 
@RequestMapping("/api/customers")
public class CustomerController {
    
    @Autowired 
    private CustomerRepository repository;

    @GetMapping
    public List<Customer> findAll(){
        return repository.findAll();
    }
}
