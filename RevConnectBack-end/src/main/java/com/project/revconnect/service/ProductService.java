package com.project.revconnect.service;

import com.project.revconnect.enums.Handlers;
import com.project.revconnect.model.Product;
import com.project.revconnect.model.User;
import com.project.revconnect.repository.ProductRepository;
import com.project.revconnect.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository repository;
    private final UserRepository userRepository;

    public ProductService(ProductRepository repository,
                          UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    private User getBusinessUser() {
        User user = com.project.revconnect.util.AuthUtil.getLoggedInUser(userRepository);

        if (user == null || user.getRole() != Handlers.Business_Account_User) {
            throw new RuntimeException("Only Business Users allowed");
        }

        return user;
    }

    public Product add(Product product) {
        User user = getBusinessUser();
        sanitizeProduct(product);
        product.setUser(user);
        return repository.save(product);
    }

    public List<Product> myProducts() {
        return repository.findByUser(getBusinessUser());
    }

    public List<Product> getProductsByUserId(Long userId) {
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return repository.findByUser(targetUser);
    }

    public Product update(Long id, Product updated) {
        User user = getBusinessUser();
        Product product = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!product.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Not your product");
        }

        sanitizeProduct(updated);
        product.setProductName(updated.getProductName());
        product.setDescription(updated.getDescription());
        product.setPrice(updated.getPrice());
        product.setImageUrl(updated.getImageUrl());
        product.setStock(updated.getStock());
        product.setExternalLink(updated.getExternalLink());
        product.setFeatures(updated.getFeatures());

        return repository.save(product);
    }

    public String delete(Long id) {
        User user = getBusinessUser();
        Product product = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!product.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Not your product");
        }

        repository.delete(product);
        return "Product Deleted Successfully";
    }

    private void sanitizeProduct(Product product) {
        if (product == null) {
            throw new RuntimeException("Invalid product payload");
        }

        String name = sanitizeText(product.getProductName(), 255);
        if (name == null) {
            throw new RuntimeException("Product name is required");
        }

        double price = product.getPrice();
        if (!Double.isFinite(price) || price < 0) {
            throw new RuntimeException("Price must be a non-negative number");
        }

        int stock = product.getStock();
        if (stock < 0) {
            throw new RuntimeException("Stock cannot be negative");
        }

        product.setProductName(name);
        product.setDescription(sanitizeText(product.getDescription(), 2000));
        product.setImageUrl(sanitizeText(product.getImageUrl(), 2000));
        product.setExternalLink(normalizeExternalLink(product.getExternalLink()));
        product.setFeatures(normalizeFeatures(product.getFeatures()));
    }

    private String normalizeExternalLink(String externalLink) {
        String sanitized = sanitizeText(externalLink, 2000);
        if (sanitized == null) {
            return null;
        }

        String normalized = sanitized;
        if (!normalized.matches("^[a-zA-Z][a-zA-Z0-9+.-]*://.*$")) {
            normalized = "https://" + normalized;
        }

        try {
            URI uri = new URI(normalized);
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new RuntimeException("External link must use http or https");
            }
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                throw new RuntimeException("External link is not valid");
            }
            return uri.toASCIIString();
        } catch (URISyntaxException ex) {
            throw new RuntimeException("External link is not valid");
        }
    }

    private String normalizeFeatures(String features) {
        String sanitized = sanitizeText(features, 2000);
        if (sanitized == null) {
            return null;
        }

        String[] chunks = sanitized.split("[\\r\\n,]+");
        StringBuilder builder = new StringBuilder();
        int count = 0;

        for (String chunk : chunks) {
            String item = sanitizeText(chunk, 120);
            if (item == null) {
                continue;
            }

            if (count == 12) {
                break;
            }

            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append(item);
            count++;
        }

        return builder.length() == 0 ? null : builder.toString();
    }

    private String sanitizeText(String value, int maxLength) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        if (trimmed.length() > maxLength) {
            return trimmed.substring(0, maxLength);
        }

        return trimmed;
    }
}
