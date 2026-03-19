package com.project.revconnect.dto;

public class AccountDetailsUpdateDTO {
    private String username;
    private String email;

    public AccountDetailsUpdateDTO() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
