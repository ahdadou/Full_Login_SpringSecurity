package com.login.demo.dto;

import java.util.List;


public class UserDto {
	
	    private String username;

	    private String email;

	    private String password;

	    private Boolean registerAsAdmin;

	    public UserDto(String username, String email,
	                               String password, Boolean registerAsAdmin) {
	        this.username = username;
	        this.email = email;
	        this.password = password;
	        this.registerAsAdmin = registerAsAdmin;
	    }

	    public UserDto() {
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


