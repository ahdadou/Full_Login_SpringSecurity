package com.login.demo.dto;


import javax.validation.constraints.NotNull;

public class RegisterDto {

    private String username;

    private String email;

    @NotNull(message = "Registration password cannot be null")
    private String password;

    @NotNull(message = "Specify whether the user has to be registered as an admin or not")
    private Boolean registerAsAdmin;

    public RegisterDto(String username, String email,
                               String password, Boolean registerAsAdmin) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.registerAsAdmin = registerAsAdmin;
    }

    public RegisterDto() {
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getRegisterAsAdmin() {
        return registerAsAdmin;
    }

    public void setRegisterAsAdmin(Boolean registerAsAdmin) {
        this.registerAsAdmin = registerAsAdmin;
    }
}
