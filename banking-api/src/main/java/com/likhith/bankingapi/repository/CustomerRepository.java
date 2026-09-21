package com.likhith.bankingapi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.likhith.bankingapi.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByCustomerId(String customerId);

    boolean existsByCustomerId(String customerId);
}
