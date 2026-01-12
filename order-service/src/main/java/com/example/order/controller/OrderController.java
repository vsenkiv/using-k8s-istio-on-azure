package com.example.order.controller;

import com.example.order.service.ProductClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

  @Autowired
  private ProductClient productClient;

  @GetMapping("/test")
  public ResponseEntity<Map<String, Object>> testEndpoint() {
    Map<String, Object> response = new HashMap<>();
    response.put("service", "order-service");
    response.put("version", getVersion());
    response.put("status", "healthy");
    response.put("timestamp", System.currentTimeMillis());

    return ResponseEntity.ok(response);
  }

  @PostMapping("/create")
  public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Map<String, Object> orderRequest) {
    String productId = (String) orderRequest.get("productId");
    Integer quantity = (Integer) orderRequest.get("quantity");

    Map<String, Object> product = productClient.getProduct(productId);

    Map<String, Object> order = new HashMap<>();
    order.put("orderId", "ORD-" + System.currentTimeMillis());
    order.put("productId", productId);
    order.put("productName", product.get("name"));
    order.put("quantity", quantity);
    order.put("price", product.get("price"));
    order.put("total", (Double) product.get("price") * quantity);
    order.put("status", "created");
    order.put("serviceVersion", getVersion());
    order.put("timestamp", System.currentTimeMillis());

    return ResponseEntity.ok(order);
  }

  @GetMapping("/health")
  public ResponseEntity<Map<String, String>> health() {
    Map<String, String> health = new HashMap<>();
    health.put("status", "UP");
    health.put("version", getVersion());
    return ResponseEntity.ok(health);
  }

  private String getVersion() {
    return System.getenv().getOrDefault("SERVICE_VERSION", "v1");
  }
}