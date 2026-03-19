package com.project.revconnect.dto;

public class ProductRequestDTO {

    private String productName;
    private String description;
    private double price;
    private String imageUrl;
    private int stock;
    private String externalLink;
    private String features;

    public ProductRequestDTO() {}

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
}
