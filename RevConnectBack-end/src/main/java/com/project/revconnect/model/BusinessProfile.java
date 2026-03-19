package com.project.revconnect.model;

import com.project.revconnect.model.User;
import jakarta.persistence.*;

@Entity
@Table(name = "business_profile")
public class BusinessProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String businessName;
    private String businessCategory;
    private String description;
    private String website;
    private String contactEmail;
    private String contactPhone;
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String logoUrl;

    // --- Instagram Clone & Settings ---
    private String businessAddress;
    private String businessHours;

    @Column(length = 2000)
    private String externalLinks; // Comma-separated or JSON string for multiple links

    @Column(columnDefinition = "boolean default true")
    private boolean isPublic = true;

    @Column(columnDefinition = "boolean default true")
    private boolean allowMessages = true;

    @Column(nullable = false)
    private boolean verified = false;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private com.project.revconnect.model.User user;

    public BusinessProfile() {
        this.verified = false;
    }


    public Long getId() {
        return id;
    }

    public String getBusinessName() {
        return businessName;
    }

    public String getDescription() {
        return description;
    }

    public String getBusinessCategory() {
        return businessCategory;
    }

    public String getWebsite() {
        return website;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public String getBusinessAddress() {
        return businessAddress;
    }

    public String getBusinessHours() {
        return businessHours;
    }

    public String getExternalLinks() {
        return externalLinks;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public boolean isAllowMessages() {
        return allowMessages;
    }

    public boolean isVerified() {
        return verified;
    }

    public com.project.revconnect.model.User getUser() {
        return user;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public void setBusinessCategory(String businessCategory) {
        this.businessCategory = businessCategory;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public void setBusinessAddress(String businessAddress) {
        this.businessAddress = businessAddress;
    }

    public void setBusinessHours(String businessHours) {
        this.businessHours = businessHours;
    }

    public void setExternalLinks(String externalLinks) {
        this.externalLinks = externalLinks;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public void setAllowMessages(boolean allowMessages) {
        this.allowMessages = allowMessages;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setId(long l) {

    }
}
