package com.inventory.wareflow.service;

import com.inventory.wareflow.dto.SupplierRequest;
import com.inventory.wareflow.entity.Supplier;
import com.inventory.wareflow.exception.ResourceNotFoundException;
import com.inventory.wareflow.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public List<Supplier> listAll() {
        return supplierRepository.findAll();
    }

    public Supplier getById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
    }

    public Supplier create(SupplierRequest request) {
        Supplier supplier = Supplier.builder()
                .name(request.getName())
                .contactEmail(request.getContactEmail())
                .contactPhone(request.getContactPhone())
                .address(request.getAddress())
                .build();
        return supplierRepository.save(supplier);
    }

    public Supplier update(Long id, SupplierRequest request) {
        Supplier supplier = getById(id);
        supplier.setName(request.getName());
        supplier.setContactEmail(request.getContactEmail());
        supplier.setContactPhone(request.getContactPhone());
        supplier.setAddress(request.getAddress());
        return supplierRepository.save(supplier);
    }

    public void delete(Long id) {
        Supplier supplier = getById(id);
        supplierRepository.delete(supplier);
    }
}