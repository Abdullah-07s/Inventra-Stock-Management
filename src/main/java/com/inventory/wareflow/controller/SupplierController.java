package com.inventory.wareflow.controller;

import com.inventory.wareflow.dto.SupplierRequest;
import com.inventory.wareflow.entity.Supplier;
import com.inventory.wareflow.enums.Activity;
import com.inventory.wareflow.security.RequiresActivity;
import com.inventory.wareflow.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    public List<Supplier> listAll() {
        return supplierService.listAll();
    }

    @GetMapping("/{id}")
    public Supplier getById(@PathVariable Long id) {
        return supplierService.getById(id);
    }

    @PostMapping
    @RequiresActivity(Activity.MANAGE_SUPPLIERS)
    public ResponseEntity<Supplier> create(@Valid @RequestBody SupplierRequest request) {
        Supplier created = supplierService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @RequiresActivity(Activity.MANAGE_SUPPLIERS)
    public Supplier update(@PathVariable Long id, @Valid @RequestBody SupplierRequest request) {
        return supplierService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @RequiresActivity(Activity.MANAGE_SUPPLIERS)
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        supplierService.delete(id);
        return ResponseEntity.noContent().build();
    }
}