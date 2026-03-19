package com.project.revconnect.dto;

public class BusinessAnalyticsDTO {

    private long followers;
    private long totalPosts;
    private long totalLikes;
    private long totalComments;
    private long totalProducts;
    private long totalReviews;

    public BusinessAnalyticsDTO(long followers,
                                long totalPosts,
                                long totalLikes,
                                long totalComments,
                                long totalProducts,
                                long totalReviews) {
        this.followers = followers;
        this.totalPosts = totalPosts;
        this.totalLikes = totalLikes;
        this.totalComments = totalComments;
        this.totalProducts = totalProducts;
        this.totalReviews = totalReviews;
    }

    public long getFollowers() { return followers; }
    public long getTotalPosts() { return totalPosts; }
    public long getTotalLikes() { return totalLikes; }
    public long getTotalComments() { return totalComments; }
    public long getTotalProducts() { return totalProducts; }
    public long getTotalReviews() { return totalReviews; }
}