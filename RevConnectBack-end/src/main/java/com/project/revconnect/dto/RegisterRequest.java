package com.project.revconnect.dto;

import com.project.revconnect.enums.Handlers;

public class RegisterRequest {

    private String email;
    private String username;
    private String password;
    private String confirmPassword;
    private Handlers role;

    public RegisterRequest() {}

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public Handlers getRole() {
        return role;
    }

    public void setRole(Handlers role) {
        this.role = role;
    }
}

