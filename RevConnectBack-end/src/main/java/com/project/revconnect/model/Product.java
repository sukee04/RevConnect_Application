package com.project.revconnect.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.revconnect.model.User;
import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String productName;

    @Column(length = 2000)
    private String description;

    private double price;

    @Column(length = 2000)
    private String imageUrl;

    private int stock;

    @Column(length = 2000)
    private String externalLink;

    @Column(length = 2000)
    private String features;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private com.project.revconnect.model.User user;

    @Transient
    private Long userId;

    public Product() {
    }

    @PostLoad
    @PostPersist
    public void populateTransient() {
        if (user != null) {
            this.userId = user.getId();
        }
    }

    public Long getId() {
        return id;
    }

    public String getProductName() {
        return productName;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getStock() {
        return stock;
    }

    public String getExternalLink() {
        return externalLink;
    }

    public String getFeatures() {
        return features;
    }

    public com.project.revconnect.model.User getUser() {
        return user;
    }

    public Long getUserId() {
        return userId;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public void setExternalLink(String externalLink) {
        this.externalLink = externalLink;
    }

    public void setFeatures(String features) {
        this.features = features;
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            this.userId = user.getId();
        }
    }
}
