package com.example.customerservice.service;

import com.example.customerservice.entity.CustomerEntity;
import com.example.customerservice.repository.CustomerRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Transactional(readOnly = true)
    public List<CustomerEntity> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public CustomerEntity getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public CustomerEntity getCustomerByEmail(String email) {
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found with email: " + email));
    }

    @Transactional(readOnly = true)
    public List<CustomerEntity> getCustomersByName(String name) {
        return customerRepository.searchCustomers(name);
    }

    @Transactional(readOnly = true)
    public List<CustomerEntity> searchCustomers(String nameOrEmail) {
        return customerRepository.searchCustomers(nameOrEmail);
    }

    @Transactional
    public CustomerEntity createCustomer(CustomerEntity customer) {
        return customerRepository.save(customer);
    }

    @Transactional
    public CustomerEntity updateCustomer(Long id, CustomerEntity customer) {
        // 기존 고객 조회
        CustomerEntity existingCustomer = getCustomerById(id);

        // 새로운 불변 객체 생성 (Builder 패턴 사용)
        CustomerEntity updatedCustomer = CustomerEntity.builder()
                .name(customer.getName() != null ? customer.getName() : existingCustomer.getName())
                .email(customer.getEmail() != null ? customer.getEmail() : existingCustomer.getEmail())
                .address(customer.getAddress() != null ? customer.getAddress() : existingCustomer.getAddress())
                .phoneNumber(customer.getPhoneNumber() != null ? customer.getPhoneNumber()
                        : existingCustomer.getPhoneNumber())
                .build();

        // ID 값 설정을 위한 리플렉션 사용 대신 새 객체로 저장
        customerRepository.delete(existingCustomer);
        return customerRepository.save(updatedCustomer);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        CustomerEntity customer = getCustomerById(id);
        customerRepository.delete(customer);
    }
}