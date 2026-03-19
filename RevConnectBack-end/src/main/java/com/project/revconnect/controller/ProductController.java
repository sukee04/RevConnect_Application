package com.project.revconnect.controller;

import com.project.revconnect.model.Product;
import com.project.revconnect.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping({"/business/products", "/products"})
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @PostMapping({"", "/add"})
    public ResponseEntity<?> add(@RequestBody Product product) {
        try {
            return ResponseEntity.ok(service.add(product));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping({"", "/my"})
    public ResponseEntity<?> list() {
        try {
            return ResponseEntity.ok(service.myProducts());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getProductsByUser(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(service.getProductsByUserId(userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Product product) {
        try {
            return ResponseEntity.ok(service.update(id, product));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping({"/{id}", "/delete/{id}"})
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.delete(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
