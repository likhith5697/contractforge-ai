package com.likhith.bankingapi.service;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.likhith.bankingapi.common.IdGenerator;
import com.likhith.bankingapi.dto.request.AddAddressRequest;
import com.likhith.bankingapi.dto.request.CreateCustomerRequest;
import com.likhith.bankingapi.dto.response.AddressResponse;
import com.likhith.bankingapi.dto.response.CustomerResponse;
import com.likhith.bankingapi.entity.Address;
import com.likhith.bankingapi.entity.Customer;
import com.likhith.bankingapi.entity.enums.CustomerEnums.KycStatus;
import com.likhith.bankingapi.entity.enums.CustomerEnums.OnboardingStatus;
import com.likhith.bankingapi.entity.enums.CustomerEnums.RiskRating;
import com.likhith.bankingapi.exception.ResourceNotFoundException;
import com.likhith.bankingapi.repository.AddressRepository;
import com.likhith.bankingapi.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;

    @Transactional
    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        Customer customer = Customer.builder()
                .customerId(IdGenerator.generate("CUS"))
                .firstName(request.getPersonalInfo().getFirstName())
                .lastName(request.getPersonalInfo().getLastName())
                .dateOfBirth(request.getPersonalInfo().getDateOfBirth())
                .gender(request.getPersonalInfo().getGender())
                .nationality(request.getPersonalInfo().getNationality())
                .email(request.getContactInfo().getEmail())
                .phoneNumber(request.getContactInfo().getPhoneNumber())
                .alternatePhoneNumber(request.getContactInfo().getAlternatePhoneNumber())
                .documentType(request.getIdentityDocument().getDocumentType())
                .documentNumber(request.getIdentityDocument().getDocumentNumber())
                .documentIssuingCountry(request.getIdentityDocument().getIssuingCountry())
                .documentExpiryDate(request.getIdentityDocument().getExpiryDate())
                .occupation(request.getEmploymentInfo().getOccupation())
                .employerName(request.getEmploymentInfo().getEmployerName())
                .employmentType(request.getEmploymentInfo().getEmploymentType())
                .annualIncome(request.getEmploymentInfo().getAnnualIncome())
                .addressLine1(request.getResidentialAddress().getAddressLine1())
                .city(request.getResidentialAddress().getCity())
                .state(request.getResidentialAddress().getState())
                .postalCode(request.getResidentialAddress().getPostalCode())
                .country(request.getResidentialAddress().getCountry())
                .sourceOfFunds(request.getRiskProfile().getSourceOfFunds())
                .expectedMonthlyTurnover(request.getRiskProfile().getExpectedMonthlyTurnover())
                .kycStatus(KycStatus.NOT_STARTED)
                .riskRating(RiskRating.LOW)
                .onboardingStatus(OnboardingStatus.PENDING_KYC)
                .createdAt(Instant.now())
                .build();

        customer = customerRepository.save(customer);
        log.info("Onboarded new customer {}", customer.getCustomerId());

        return CustomerResponse.builder()
                .customerId(customer.getCustomerId())
                .fullName(customer.getFirstName() + " " + customer.getLastName())
                .email(customer.getEmail())
                .kycStatus(customer.getKycStatus())
                .riskRating(customer.getRiskRating())
                .onboardingStatus(customer.getOnboardingStatus())
                .createdAt(customer.getCreatedAt())
                .build();
    }

    @Transactional
    public AddressResponse addAddress(String customerId, AddAddressRequest request) {
        Customer customer = customerRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("CUSTOMER_NOT_FOUND",
                        "Customer '" + customerId + "' does not exist"));

        Address address = Address.builder()
                .addressId(IdGenerator.generate("ADR"))
                .customer(customer)
                .addressType(request.getAddressType())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .primary(Boolean.TRUE.equals(request.getIsPrimary()))
                .effectiveFrom(request.getEffectiveFrom())
                .latitude(request.getGeoLocation() != null ? request.getGeoLocation().getLatitude() : null)
                .longitude(request.getGeoLocation() != null ? request.getGeoLocation().getLongitude() : null)
                .createdAt(Instant.now())
                .build();

        address = addressRepository.save(address);
        log.info("Added address {} for customer {}", address.getAddressId(), customerId);

        return AddressResponse.builder()
                .addressId(address.getAddressId())
                .customerId(customerId)
                .addressType(address.getAddressType())
                .addressLine1(address.getAddressLine1())
                .city(address.getCity())
                .country(address.getCountry())
                .primary(address.isPrimary())
                .effectiveFrom(address.getEffectiveFrom())
                .createdAt(address.getCreatedAt())
                .build();
    }
}
