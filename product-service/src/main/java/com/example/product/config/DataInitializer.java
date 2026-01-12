package com.example.product.config;

import com.example.product.entity.Product;
import com.example.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

  @Autowired
  private ProductRepository productRepository;

  @Override
  public void run(String... args) {
    if (productRepository.count() == 0) {
      productRepository.findByProductId("PROD-001").ifPresentOrElse(product -> {
          }, () -> productRepository.save(
              new Product(1l, "PROD-001", "Laptop", 999.99, "High-performance laptop", 50))
      );

      productRepository.findByProductId("PROD-002").ifPresentOrElse(product -> {
          }, () -> productRepository.save(
              new Product(2l, "PROD-002", "Mouse", 29.99, "Wireless mouse", 200))
      );

      productRepository.findByProductId("PROD-003").ifPresentOrElse(product -> {
          }, () -> productRepository.save(
              new Product(3l, "PROD-003", "Keyboard", 79.99, "Mechanical keyboard", 100))
      );
      productRepository.findByProductId("PROD-004").ifPresentOrElse(product -> {
          }, () -> productRepository.save(
              new Product(4l, "PROD-004", "Monitor", 299.99, "27-inch 4K monitor", 75))
      );
      productRepository.findByProductId("PROD-005").ifPresentOrElse(product -> {
          }, () -> productRepository.save(
              new Product(5l, "PROD-005", "Headphones", 149.99, "Noise-canceling headphones", 150))
      );
    }

    System.out.println("Sample products initialized!");

  }
}