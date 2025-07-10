package com.labMetricas.LabMetricas.customer.service;

import com.labMetricas.LabMetricas.EquipmentCategory.model.EquipmentCategory;
import com.labMetricas.LabMetricas.customer.model.Customer;
import com.labMetricas.LabMetricas.customer.model.dto.CustomerDto;
import com.labMetricas.LabMetricas.customer.repository.CustomerRepository;
import com.labMetricas.LabMetricas.enums.TypeResponse;
import com.labMetricas.LabMetricas.util.ResponseObject;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CustomerService {
    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public ResponseEntity<ResponseObject> createCustomer(CustomerDto customerDto) {
        try {
            // Check if email or NIF already exists
            if (customerRepository.existsByEmail(customerDto.getEmail())) {
                return ResponseEntity.badRequest().body(
                    new ResponseObject("Email already exists", null, TypeResponse.ERROR)
                );
            }
            if (customerRepository.existsByNif(customerDto.getNif())) {
                return ResponseEntity.badRequest().body(
                    new ResponseObject("NIF already exists", null, TypeResponse.ERROR)
                );
            }

            // Create new customer
            Customer customer = new Customer();
            customer.setName(customerDto.getName());
            customer.setEmail(customerDto.getEmail());
            customer.setNif(customerDto.getNif());
            
            // Add address and phone fields
            customer.setAddress(customerDto.getAddress());
            customer.setPhone(customerDto.getPhone());
            
            // Always set status to true when creating
            customer.setStatus(true);
            
            customer.setCreatedAt(LocalDateTime.now());
            customer.setLastModification(LocalDateTime.now());

            Customer savedCustomer = customerRepository.save(customer);
            logger.info("Customer created successfully: {}", savedCustomer.getEmail());

            return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseObject("Customer created successfully", savedCustomer, TypeResponse.SUCCESS)
            );
        } catch (Exception e) {
            logger.error("Error creating customer: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ResponseObject("Error creating customer", null, TypeResponse.ERROR)
            );
        }
    }

    @Transactional
    public ResponseEntity<ResponseObject> updateCustomerByEmail(String email, CustomerDto customerDto) {
        try {
            // Find existing customer
            Customer existingCustomer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with email: " + email));

            // Check if new email is different and already exists
            if (!email.equals(customerDto.getEmail()) && 
                customerRepository.existsByEmail(customerDto.getEmail())) {
                return ResponseEntity.badRequest().body(
                    new ResponseObject("New email already exists", null, TypeResponse.ERROR)
                );
            }

            // Update customer details
            existingCustomer.setName(customerDto.getName());
            existingCustomer.setEmail(customerDto.getEmail());
            existingCustomer.setNif(customerDto.getNif());
            
            // Update address and phone fields
            existingCustomer.setAddress(customerDto.getAddress());
            existingCustomer.setPhone(customerDto.getPhone());
            
            // Preserve existing status
            // existingCustomer.setStatus(customerDto.getStatus());
            
            existingCustomer.setLastModification(LocalDateTime.now());

            Customer updatedCustomer = customerRepository.save(existingCustomer);
            logger.info("Customer updated successfully: {}", updatedCustomer.getEmail());

            return ResponseEntity.ok(
                new ResponseObject("Customer updated successfully", updatedCustomer, TypeResponse.SUCCESS)
            );
        } catch (EntityNotFoundException e) {
            logger.error("Customer not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ResponseObject(e.getMessage(), null, TypeResponse.ERROR)
            );
        } catch (Exception e) {
            logger.error("Error updating customer: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ResponseObject("Error updating customer", null, TypeResponse.ERROR)
            );
        }
    }

    @Transactional
    public ResponseEntity<ResponseObject> deleteCustomerByEmail(String email) {
        try {
            // Find existing customer
            Customer existingCustomer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with email: " + email));

            // Change status to false (soft delete)
            existingCustomer.setStatus(false);
            existingCustomer.setLastModification(LocalDateTime.now());
            
            // Save the updated customer
            Customer updatedCustomer = customerRepository.save(existingCustomer);
            
            logger.info("Customer status updated to inactive: {}", email);

            return ResponseEntity.ok(
                new ResponseObject("Customer marked as inactive", updatedCustomer, TypeResponse.SUCCESS)
            );
        } catch (EntityNotFoundException e) {
            logger.error("Customer not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ResponseObject(e.getMessage(), null, TypeResponse.ERROR)
            );
        } catch (Exception e) {
            logger.error("Error updating customer status: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ResponseObject("Error updating customer status", null, TypeResponse.ERROR)
            );
        }
    }

    // Toggle Equipment Category status
    @Transactional
    public ResponseEntity<ResponseObject> toggleCustomerStatus(String email) {
        try {
            // Find existing customer
            Customer existingCustomer = customerRepository.findByEmail(email)
                    .orElseThrow(() -> new EntityNotFoundException("Customer not found with email: " + email));

            // Change status to false (soft delete)
            existingCustomer.setStatus(!existingCustomer.getStatus());
            existingCustomer.setLastModification(LocalDateTime.now());

            // Save the updated customer
            Customer updatedCustomer = customerRepository.saveAndFlush(existingCustomer);

            logger.info("Customer status updated to inactive: {}", email);

            return ResponseEntity.ok(
                    new ResponseObject("Customer status toggled successfully", updatedCustomer, TypeResponse.SUCCESS)
            );
        } catch (EntityNotFoundException e) {
            logger.error("Customer not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ResponseObject(e.getMessage(), null, TypeResponse.ERROR)
            );
        } catch (Exception e) {
            logger.error("Error updating customer status: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ResponseObject("Error updating customer status", null, TypeResponse.ERROR)
            );
        }
    }

    // Add a method to reactivate a customer
    @Transactional
    public ResponseEntity<ResponseObject> reactivateCustomerByEmail(String email) {
        try {
            // Find existing customer
            Customer existingCustomer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with email: " + email));

            // Change status to true (reactivate)
            existingCustomer.setStatus(true);
            existingCustomer.setLastModification(LocalDateTime.now());
            
            // Save the updated customer
            Customer updatedCustomer = customerRepository.save(existingCustomer);
            
            logger.info("Customer status updated to active: {}", email);

            return ResponseEntity.ok(
                new ResponseObject("Customer reactivated successfully", updatedCustomer, TypeResponse.SUCCESS)
            );
        } catch (EntityNotFoundException e) {
            logger.error("Customer not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ResponseObject(e.getMessage(), null, TypeResponse.ERROR)
            );
        } catch (Exception e) {
            logger.error("Error reactivating customer: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ResponseObject("Error reactivating customer", null, TypeResponse.ERROR)
            );
        }
    }

    public ResponseEntity<ResponseObject> getCustomerByEmail(String email) {
        try {
            Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with email: " + email));

            return ResponseEntity.ok(
                new ResponseObject("Customer retrieved successfully", customer, TypeResponse.SUCCESS)
            );
        } catch (EntityNotFoundException e) {
            logger.error("Customer not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ResponseObject(e.getMessage(), null, TypeResponse.ERROR)
            );
        } catch (Exception e) {
            logger.error("Error retrieving customer: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ResponseObject("Error retrieving customer", null, TypeResponse.ERROR)
            );
        }
    }

    // Modify getAllCustomers to only return active customers by default
    public ResponseEntity<ResponseObject> getAllCustomers() {
        try {
            // Fetch only active customers
            List<Customer> activeCustomers = customerRepository.findAll();
            logger.info("Retrieved {} customers", activeCustomers.size());

            return ResponseEntity.ok(
                new ResponseObject("Customers retrieved successfully", activeCustomers, TypeResponse.SUCCESS)
            );
        } catch (Exception e) {
            logger.error("Error retrieving customers: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ResponseObject("Error retrieving customers", null, TypeResponse.ERROR)
            );
        }
    }

    // Add a method to get all customers including inactive ones
    public ResponseEntity<ResponseObject> getAllCustomersIncludingInactive() {
        try {
            List<Customer> customers = customerRepository.findAll();
            logger.info("Retrieved {} customers (including inactive)", customers.size());

            return ResponseEntity.ok(
                new ResponseObject("All customers retrieved successfully", customers, TypeResponse.SUCCESS)
            );
        } catch (Exception e) {
            logger.error("Error retrieving all customers: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ResponseObject("Error retrieving all customers", null, TypeResponse.ERROR)
            );
        }
    }
} 