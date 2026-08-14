package com.inventory.wareflow.service;

import com.inventory.wareflow.dto.ProductRequest;
import com.inventory.wareflow.entity.Category;
import com.inventory.wareflow.entity.Product;
import com.inventory.wareflow.entity.Supplier;
import com.inventory.wareflow.exception.AuthException;
import com.inventory.wareflow.exception.ResourceNotFoundException;
import com.inventory.wareflow.repository.CategoryRepository;
import com.inventory.wareflow.repository.OrderItemRepository;
import com.inventory.wareflow.repository.ProductRepository;
import com.inventory.wareflow.repository.StockRecordRepository;
import com.inventory.wareflow.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final StockRecordRepository stockRecordRepository;
    private final OrderItemRepository orderItemRepository;

    public List<Product> listAll() {
        return productRepository.findAll();
    }

    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    public Product create(ProductRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new AuthException("A product with this SKU already exists", HttpStatus.BAD_REQUEST);
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Supplier not found with id: " + request.getSupplierId()));

        Product product = Product.builder()
                .sku(request.getSku())
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(category)
                .supplier(supplier)
                .build();

        return productRepository.save(product);
    }

    public Product update(Long id, ProductRequest request) {
        Product product = getById(id);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Supplier not found with id: " + request.getSupplierId()));

        product.setSku(request.getSku());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCategory(category);
        product.setSupplier(supplier);

        return productRepository.save(product);
    }

    @Transactional
    // @Transactional ensures the cascade delete (stock records + order items +
    // the product itself) happens atomically - if any step fails, nothing is
    // deleted.
    public void delete(Long id) {
        Product product = getById(id);
        stockRecordRepository.deleteByProduct(product);
        orderItemRepository.deleteByProduct(product);
        productRepository.delete(product);
    }
}