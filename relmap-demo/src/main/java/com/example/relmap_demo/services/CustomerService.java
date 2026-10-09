package com.example.relmap_demo.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.relmap_demo.daos.CustomerRepository;
import com.example.relmap_demo.models.Customer;
import org.springframework.transaction.annotation.*;
@Service 
public class CustomerService {
    
    @Autowired 
    private CustomerRepository repository;

    @Transactional
    public Customer save(Customer customer){
        Customer c = repository.save(customer);
        return c;
    }

    //@Transactional(propagation = Propagation.NEVER)
    public List<Customer> loadAll(){
        return repository.findAll();
    }
}
