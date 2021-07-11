package com.login.demo.dto;

import javax.validation.Valid;

public class LoginDto {
	
	    private String username;

	    private String email;

	    
	    private String password;


	    
	    

	    public LoginDto(String username, String email, String password) {
			super();
			this.username = username;
			this.email = email;
			this.password = password;
		}

		public LoginDto() {
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

	    
}
