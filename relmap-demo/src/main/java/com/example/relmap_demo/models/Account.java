package com.example.relmap_demo.models;

import jakarta.persistence.*;

@Entity 
@Table (name="accounts")
public class Account {
    
    @Id
    @Column(name="acc_no")
    private String accountNumber;

    @Column(name="acc_type", length = 2)
    private String accType;

    @Column(name="acc_balance")
    private Double balance;

    @ManyToOne 
    @JoinColumn(name="acc_holder")
    private Customer customer;

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccType() {
        return accType;
    }

    public void setAccType(String accType) {
        this.accType = accType;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Account() {
    }

    public Account(String accountNumber, String accType, Double balance, Customer customer) {
        this.accountNumber = accountNumber;
        this.accType = accType;
        this.balance = balance;
        this.customer = customer;
    }

    
}
