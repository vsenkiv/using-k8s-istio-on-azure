package com.example.product.controller;

import com.example.product.entity.Product;
import com.example.product.repository.ProductRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {

  @Autowired
  private ProductRepository productRepository;

  @GetMapping("/{productId}")
  public ResponseEntity<Map<String, Object>> getProduct(@PathVariable String productId) {
    Product product = productRepository.findByProductId(productId)
        .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

    Map<String, Object> response = new HashMap<>();
    response.put("productId", product.getProductId());
    response.put("name", product.getName());
    response.put("price", product.getPrice());
    response.put("description", product.getDescription());
    response.put("stock", product.getStock());
    response.put("serviceVersion", getVersion());
    response.put("timestamp", System.currentTimeMillis());

    return ResponseEntity.ok(response);
  }

  @GetMapping
  public ResponseEntity<List<Product>> getAllProducts() {
    return ResponseEntity.ok(productRepository.findAll());
  }

  @PostMapping
  public ResponseEntity<Product> createProduct(@RequestBody Product product) {
    Product saved = productRepository.save(product);
    return ResponseEntity.ok(saved);
  }

  @GetMapping("/health")
  public ResponseEntity<Map<String, String>> health() {
    Map<String, String> health = new HashMap<>();
    health.put("status", "UP");
    health.put("version", getVersion());
    health.put("database", "connected");
    return ResponseEntity.ok(health);
  }

  private String getVersion() {
    return System.getenv().getOrDefault("SERVICE_VERSION", "v1");
  }
}