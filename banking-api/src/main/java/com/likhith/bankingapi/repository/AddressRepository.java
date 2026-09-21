package com.likhith.bankingapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.likhith.bankingapi.entity.Address;

public interface AddressRepository extends JpaRepository<Address, Long> {
}
