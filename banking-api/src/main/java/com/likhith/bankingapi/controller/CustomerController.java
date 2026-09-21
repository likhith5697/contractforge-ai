package com.likhith.bankingapi.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.likhith.bankingapi.common.ApiResponse;
import com.likhith.bankingapi.dto.request.AddAddressRequest;
import com.likhith.bankingapi.dto.request.CreateCustomerRequest;
import com.likhith.bankingapi.dto.response.AddressResponse;
import com.likhith.bankingapi.dto.response.CustomerResponse;
import com.likhith.bankingapi.service.CustomerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "Customers", description = "Customer onboarding and address management")
public class CustomerController {

    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);

    private final CustomerService customerService;

    @PostMapping
    @Operation(summary = "Onboard a new customer")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Customer created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failure")
    })
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(
            @Valid @RequestBody CreateCustomerRequest request) {
        log.info("Received customer onboarding request for email {}", request.getContactInfo().getEmail());
        CustomerResponse response = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Customer onboarded successfully", response));
    }

    @PostMapping("/{customerId}/addresses")
    @Operation(summary = "Add an address to an existing customer")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Address created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failure"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<ApiResponse<AddressResponse>> addAddress(
            @PathVariable String customerId,
            @Valid @RequestBody AddAddressRequest request) {
        log.info("Received add-address request for customer {}", customerId);
        AddressResponse response = customerService.addAddress(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Address added successfully", response));
    }
}
