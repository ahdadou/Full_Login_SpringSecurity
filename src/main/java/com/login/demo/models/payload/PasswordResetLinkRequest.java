package com.login.demo.models.payload;

public class PasswordResetLinkRequest {
	
	
    private String email;

    
	public PasswordResetLinkRequest(String email) {
		super();
		this.email = email;
	}

	
	public PasswordResetLinkRequest() {
		super();
	}
	

	public String getEmail() {
		return email;
	}
	

	public void setEmail(String email) {
		this.email = email;
	}

}
