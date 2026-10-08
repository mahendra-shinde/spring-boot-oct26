package com.example.relmap_demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.relmap_demo.daos.CustomerRepository;
import com.example.relmap_demo.models.Customer;
import com.example.relmap_demo.models.CustomerAddress;

@SpringBootApplication
public class RelmapDemoApplication  implements  CommandLineRunner{


	@Autowired 
	private CustomerRepository repository;

	public static void main(String[] args) {

		
		SpringApplication.run(RelmapDemoApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		Customer cust = new Customer();
		cust.setId(1010L);
		cust.setName("Bruce Lee");
		cust.setAddress(new CustomerAddress(020L, "Wall Street","Hong kong","Hong Kong SAR","4368746"));
		cust.setEmail("lee@gmail.com");

		repository.save(cust);
	}

	
}
