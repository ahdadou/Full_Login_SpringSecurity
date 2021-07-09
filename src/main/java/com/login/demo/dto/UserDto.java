package com.login.demo.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UserDto {
	
	private String username;
	private String password;
	private String email;
	private List<String> role;
	
	
	
	
	
	
	
	public UserDto() {
		super();
	}







	public UserDto(String username, String password, String email, List<String> role) {
		super();
		this.username = username;
		this.password = password;
		this.email = email;
		this.role = role;
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







	public String getEmail() {
		return email;
	}







	public void setEmail(String email) {
		this.email = email;
	}







	public List<String> getRole() {
		return role;
	}







	public void setRole(List<String> role) {
		this.role = role;
	}







	@Override
	public String toString() {
		return "UserDto [username=" + username + ", password=" + password + ", email=" + email + ", role=" + role + "]";
	}
	
	
}


