package com.inventory.wareflow.seed;

import com.inventory.wareflow.entity.*;
import com.inventory.wareflow.enums.Role;
import com.inventory.wareflow.repository.*;
import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * One-time seed data importer. Reads the bundled Superstore Sales CSV and
 * populates Category, Supplier, Product, StockRecord, User, Order, and
 * OrderItem tables with realistic, internally-consistent data.
 *
 * Suppliers are synthesized (the source dataset has no native supplier field)
 * and deterministically assigned per product sub-category, so the same
 * sub-category always maps to the same supplier rather than random noise.
 *
 * SAFETY: only runs when "--seed" is passed as a program argument, AND only
 * if the users table is currently empty - prevents accidental duplicate
 * imports.
 *
 * DEV-ONLY CREDENTIALS: all seeded customer accounts share the password
 * "Password123!" purely for local testing. The seeded SUPERADMIN account
 * uses "SuperAdmin123!". Neither is meant for anything beyond local dev -
 * change them immediately if this ever runs against a non-local database.
 */
@Component
// @Component registers this as a Spring-managed bean so CommandLineRunner is
// picked up on boot.
@RequiredArgsConstructor
// @RequiredArgsConstructor generates a constructor for all final fields -
// Spring uses it
// to inject the repositories below via constructor injection.
public class SeedRunner implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final StockRecordRepository stockRecordRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    private static final String CSV_PATH = "seed/superstore-sales-seed.csv";
    private static final DateTimeFormatter CSV_DATE_FORMAT = DateTimeFormatter.ofPattern("M/d/yyyy");
    private static final String[] WAREHOUSE_LOCATIONS = { "Warehouse-A", "Warehouse-B" };

    // Synthesized supplier pool - deterministically assigned per sub-category
    // below.
    private static final String[] SUPPLIER_NAMES = {
            "Meridian Office Supply Co.", "Northgate Distributors", "Apex Wholesale Group",
            "Silverline Logistics", "Harbor Point Trading", "Crestview Industrial Supply",
            "Vantage Point Distribution", "Ironwood Commercial Goods", "Bluecrest Supply Chain",
            "Redstone Wholesale Partners", "Foundry Row Distributors", "Cascade Trade Group",
            "Summit Ridge Suppliers", "Anchor Bay Commercial", "Pinehill Distribution Co.",
            "Westgate Trade Partners", "Clearwater Supply Network", "Brookstone Wholesale"
    };

    @Override
    @Transactional
    // @Transactional wraps the whole seed operation in one DB transaction -
    // if anything fails partway through, everything rolls back cleanly.
    public void run(String... args) throws Exception {
        boolean seedFlagPresent = Arrays.asList(args).contains("--seed");
        if (!seedFlagPresent) {
            return;
        }
        if (userRepository.count() > 0) {
            System.out.println("[SeedRunner] Users table is not empty - skipping seed to avoid duplicates.");
            return;
        }

        System.out.println("[SeedRunner] Starting seed import...");

        Map<String, Category> categoriesBySubCategory = new HashMap<>();
        Map<String, Supplier> suppliersByName = new HashMap<>();
        Map<String, Product> productsByName = new HashMap<>();
        Map<String, User> usersByCustomerName = new HashMap<>();
        Map<String, Order> ordersByOrderId = new LinkedHashMap<>();

        // SUPERADMIN seed account - the only login available immediately after seeding.
        User superAdmin = User.builder()
                .username("superadmin")
                .email("superadmin@wareflow.local")
                .passwordHash(BCrypt.hashpw("SuperAdmin123!", BCrypt.gensalt()))
                .role(Role.SUPERADMIN)
                .build();
        userRepository.save(superAdmin);

        try (CSVReader reader = new CSVReader(new InputStreamReader(
                new ClassPathResource(CSV_PATH).getInputStream(), StandardCharsets.UTF_8))) {

            String[] header = reader.readNext();
            // header layout: 0 Row ID, 1 Order ID, 2 Order Date, 3 Order Priority,
            // 4 Order Quantity, 5 Sales, 6 Discount, 7 Ship Mode, 8 Profit, 9 Unit Price,
            // 10 Shipping Cost, 11 Customer Name, 12 Province, 13 Region, 14 Customer
            // Segment,
            // 15 Product Category, 16 Product Sub-Category, 17 Product Name,
            // 18 Product Container, 19 Product Base Margin, 20 Ship Date

            String[] row;
            int rowsProcessed = 0;

            while ((row = reader.readNext()) != null) {
                if (row.length < 21)
                    continue;
                // Skip malformed rows rather than crashing the whole import.

                String orderId = row[1].trim();
                String orderDateRaw = row[2].trim();
                int quantity = parseIntSafe(row[4]);
                String customerName = row[11].trim();
                String productCategory = row[15].trim();
                String productSubCategory = row[16].trim();
                String productName = row[17].trim();
                BigDecimal unitPrice = parseBigDecimalSafe(row[9]);

                if (customerName.isEmpty() || productName.isEmpty() || productSubCategory.isEmpty()) {
                    continue;
                }

                // --- Category (keyed by sub-category) ---
                Category category = categoriesBySubCategory.computeIfAbsent(productSubCategory, key -> {
                    Category c = Category.builder()
                            .name(key)
                            .description("Part of the " + productCategory + " product line")
                            .build();
                    return categoryRepository.save(c);
                });

                // --- Supplier (deterministically assigned per sub-category) ---
                Supplier supplier = suppliersByName.computeIfAbsent(productSubCategory, key -> {
                    int index = Math.abs(key.hashCode()) % SUPPLIER_NAMES.length;
                    String supplierName = SUPPLIER_NAMES[index];
                    return suppliersByName.values().stream()
                            .filter(s -> s.getName().equals(supplierName))
                            .findFirst()
                            .orElseGet(() -> supplierRepository.findByName(supplierName)
                                    .orElseGet(() -> supplierRepository.save(
                                            Supplier.builder()
                                                    .name(supplierName)
                                                    .contactEmail(supplierName.toLowerCase()
                                                            .replaceAll("[^a-z0-9]+", "")
                                                            + "@supplier-contact.com")
                                                    .contactPhone("+1-555-0100")
                                                    .address("Regional Distribution Center")
                                                    .build())));
                });

                // --- Product (deduped by product name) ---
                Product product = productsByName.computeIfAbsent(productName, key -> {
                    String sku = generateSku(productCategory, key, productsByName.size());
                    Product p = Product.builder()
                            .sku(sku)
                            .name(key)
                            .description(productSubCategory + " - " + productCategory)
                            .price(unitPrice)
                            .category(category)
                            .supplier(supplier)
                            .build();
                    Product saved = productRepository.save(p);

                    // Create stock records across both warehouses with a plausible
                    // pseudo-random quantity derived from the SKU hash (deterministic, not truly
                    // random).
                    for (String warehouse : WAREHOUSE_LOCATIONS) {
                        int qty = 20 + (Math.abs((sku + warehouse).hashCode()) % 180);
                        stockRecordRepository.save(StockRecord.builder()
                                .product(saved)
                                .warehouseLocation(warehouse)
                                .quantity(qty)
                                .build());
                    }
                    return saved;
                });

                // --- User (deduped by customer name) ---
                User user = usersByCustomerName.computeIfAbsent(customerName, key -> {
                    String username = key.toLowerCase().replaceAll("[^a-z0-9]+", ".");
                    String email = username + "@example.com";
                    if (userRepository.existsByUsername(username)) {
                        return userRepository.findByUsername(username).orElseThrow();
                    }
                    return userRepository.save(User.builder()
                            .username(username)
                            .email(email)
                            .passwordHash(BCrypt.hashpw("Password123!", BCrypt.gensalt()))
                            .role(Role.USER)
                            .build());
                });

                // --- Order (grouped by Order ID) ---
                Order order = ordersByOrderId.computeIfAbsent(orderId, key -> {
                    LocalDateTime createdAt = parseOrderDate(orderDateRaw);
                    Order o = Order.builder()
                            .user(user)
                            .status(Order.OrderStatus.COMPLETED)
                            .createdAt(createdAt)
                            .build();
                    return orderRepository.save(o);
                });

                // --- OrderItem (one per CSV row) ---
                OrderItem item = OrderItem.builder()
                        .order(order)
                        .product(product)
                        .quantity(quantity)
                        .unitPriceAtPurchase(unitPrice)
                        .build();
                orderItemRepository.save(item);

                rowsProcessed++;
                if (rowsProcessed % 1000 == 0) {
                    System.out.println("[SeedRunner] Processed " + rowsProcessed + " rows...");
                }
            }

            System.out.println("[SeedRunner] Seed complete. Rows processed: " + rowsProcessed);
            System.out.println("[SeedRunner] Categories: " + categoriesBySubCategory.size());
            System.out.println("[SeedRunner] Suppliers: " + suppliersByName.size());
            System.out.println("[SeedRunner] Products: " + productsByName.size());
            System.out.println("[SeedRunner] Users (customers): " + usersByCustomerName.size());
            System.out.println("[SeedRunner] Orders: " + ordersByOrderId.size());
            System.out.println("[SeedRunner] SUPERADMIN login -> username: superadmin / password: SuperAdmin123!");
        }
    }

    private String generateSku(String category, String productName, int sequence) {
        String prefix = category.length() >= 3
                ? category.substring(0, 3).toUpperCase()
                : category.toUpperCase();
        return prefix + "-" + String.format("%05d", sequence + 1);
    }

    private LocalDateTime parseOrderDate(String raw) {
        try {
            LocalDate date = LocalDate.parse(raw, CSV_DATE_FORMAT);
            return date.atStartOfDay();
        } catch (Exception e) {
            return LocalDateTime.now();
            // Fallback for any malformed date - keeps the import from crashing on bad rows.
        }
    }

    private int parseIntSafe(String raw) {
        try {
            return Integer.parseInt(raw.trim());
        } catch (Exception e) {
            return 1;
        }
    }

    private BigDecimal parseBigDecimalSafe(String raw) {
        try {
            return new BigDecimal(raw.trim()).setScale(2, java.math.RoundingMode.HALF_UP);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}