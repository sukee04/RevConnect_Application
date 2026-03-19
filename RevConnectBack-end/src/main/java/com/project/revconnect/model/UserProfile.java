package com.project.revconnect.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.revconnect.model.User;
import jakarta.persistence.*;

@Entity
@Table(name = "userprofile")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    private String bio;
    private String location;
    private Integer age;
    private String gender;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String profilepicURL;


    @Column(name = "is_public", columnDefinition = "boolean default true")
    private boolean isPublic = true;

    @Column(columnDefinition = "boolean default true")
    private boolean allowMessages = true;

    @Column(columnDefinition = "boolean default true")
    private boolean showActivityStatus = true;


    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonIgnore
    private com.project.revconnect.model.User user;

    public UserProfile() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getProfilepicURL() { return profilepicURL; }
    public void setProfilepicURL(String profilepicURL) { this.profilepicURL = profilepicURL; }


    @JsonProperty("isPublic")
    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean aPublic) { isPublic = aPublic; }

    public boolean isAllowMessages() { return allowMessages; }
    public void setAllowMessages(boolean allowMessages) { this.allowMessages = allowMessages; }
    public boolean isShowActivityStatus() { return showActivityStatus; }
    public void setShowActivityStatus(boolean showActivityStatus) { this.showActivityStatus = showActivityStatus; }

    public com.project.revconnect.model.User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
