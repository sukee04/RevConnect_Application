package com.project.revconnect.model;

import com.project.revconnect.model.User;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "creator_profile")
public class CreatorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String displayName;

    private String bio;

    private String niche;

    private String creatorCategoryLabel;

    @Column(length = 40)
    private String profileGridLayout = "CLASSIC";

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String profilepicURL;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "creator_profile_links", joinColumns = @JoinColumn(name = "creator_profile_id"))
    @Column(name = "link_url", length = 1000)
    private List<String> linkInBioLinks = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private com.project.revconnect.model.User user;

    public CreatorProfile() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getNiche() {
        return niche;
    }

    public void setNiche(String niche) {
        this.niche = niche;
    }

    public String getCreatorCategoryLabel() {
        return creatorCategoryLabel;
    }

    public void setCreatorCategoryLabel(String creatorCategoryLabel) {
        this.creatorCategoryLabel = creatorCategoryLabel;
    }

    public String getProfileGridLayout() {
        return profileGridLayout;
    }

    public void setProfileGridLayout(String profileGridLayout) {
        this.profileGridLayout = profileGridLayout;
    }

    public String getProfilepicURL() {
        return profilepicURL;
    }

    public void setProfilepicURL(String profilepicURL) {
        this.profilepicURL = profilepicURL;
    }

    public List<String> getLinkInBioLinks() {
        return linkInBioLinks;
    }

    public void setLinkInBioLinks(List<String> linkInBioLinks) {
        this.linkInBioLinks = linkInBioLinks;
    }

    public com.project.revconnect.model.User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
