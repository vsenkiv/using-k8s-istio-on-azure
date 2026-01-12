package com.example.order.service;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class ProductClient {

  private final WebClient webClient;

  public ProductClient(
      @Value("${product.service.url:http://product-service:8080}") String productServiceUrl) {
    this.webClient = WebClient.builder()
        .baseUrl(productServiceUrl)
        .build();
  }

  public Map<String, Object> getProduct(String productId) {
    try {
      return webClient.get()
          .uri("/api/products/{id}", productId)
          .retrieve()
          .bodyToMono(Map.class)
          .block();
    } catch (Exception e) {
      Map<String, Object> fallback = new HashMap<>();
      fallback.put("productId", productId);
      fallback.put("name", "Product Not Available");
      fallback.put("price", 0.0);
      fallback.put("error", e.getMessage());
      return fallback;
    }
  }
}