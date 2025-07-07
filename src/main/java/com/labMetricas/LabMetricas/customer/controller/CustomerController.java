package com.labMetricas.LabMetricas.customer.controller;

import com.labMetricas.LabMetricas.customer.model.dto.CustomerDto;
import com.labMetricas.LabMetricas.customer.service.CustomerService;
import com.labMetricas.LabMetricas.util.ResponseObject;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CustomerController {
    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR', 'OPERADOR')")
    public ResponseEntity<ResponseObject> createCustomer(@Valid @RequestBody CustomerDto customerDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Log the attempt
        logger.info("User {} attempting to create a new customer", auth.getName());
        
        return customerService.createCustomer(customerDto);
    }

    @PutMapping("/{email}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR', 'OPERADOR')")
    public ResponseEntity<ResponseObject> updateCustomer(
        @PathVariable String email, 
        @Valid @RequestBody CustomerDto customerDto
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Log the attempt
        logger.info("User {} attempting to update customer with email {}", auth.getName(), email);
        
        return customerService.updateCustomerByEmail(email, customerDto);
    }

    @DeleteMapping("/{email}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR', 'OPERADOR')")
    public ResponseEntity<ResponseObject> deactivateCustomer(@PathVariable String email) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Log the attempt
        logger.info("User {} attempting to deactivate customer with email {}", auth.getName(), email);
        
        return customerService.deleteCustomerByEmail(email);
    }

    @PutMapping("/reactivate/{email}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR', 'OPERADOR')")
    public ResponseEntity<ResponseObject> reactivateCustomer(@PathVariable String email) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Log the attempt
        logger.info("User {} attempting to reactivate customer with email {}", auth.getName(), email);
        
        return customerService.reactivateCustomerByEmail(email);
    }

    @GetMapping("/{email}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR', 'OPERADOR')")
    public ResponseEntity<ResponseObject> getCustomerByEmail(@PathVariable String email) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Log the attempt
        logger.info("User {} attempting to retrieve customer with email {}", auth.getName(), email);
        
        return customerService.getCustomerByEmail(email);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR', 'OPERADOR')")
    public ResponseEntity<ResponseObject> getAllCustomers() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Log the attempt
        logger.info("User {} attempting to retrieve all customers", auth.getName());
        
        return customerService.getAllCustomers();
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR', 'OPERADOR')")
    public ResponseEntity<ResponseObject> getAllCustomersIncludingInactive() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Log the attempt
        logger.info("User {} attempting to retrieve all customers (including inactive)", auth.getName());
        
        return customerService.getAllCustomersIncludingInactive();
    }

    @PostMapping("/find-by-email")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR', 'OPERADOR')")
    public ResponseEntity<ResponseObject> findCustomerByEmail(@RequestBody Map<String, String> payload) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Extract email from payload
        String email = payload.get("email");
        
        // Log the attempt
        logger.info("User {} attempting to find customer with email {}", auth.getName(), email);
        
        // Validate email
        if (email == null || email.isEmpty()) {
            return customerService.getCustomerByEmail(null);
        }
        
        // Delegate to service method
        return customerService.getCustomerByEmail(email);
    }

    @PatchMapping("/{email}/toggle-status")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR', 'OPERADOR')")
    public ResponseEntity<ResponseObject> toggleCustomerStatus(@PathVariable String email) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.info("User {} attempting to deactivate customer with email {}", auth.getName(), email);

        return customerService.toggleCustomerStatus(email);
    }
} 