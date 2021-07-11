package com.login.demo.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(content = Include.NON_NULL)
public class HandleExceptionC {

	private int status;
	private String message;
	private LocalDateTime timestamp;
	private String path;
	Map<String, String> validationErrors = new HashMap<>(); // For Validators Exception
	
	public HandleExceptionC(int status, String message, LocalDateTime timestamp, String path) {
		super();
		this.status = status;
		this.message = message;
		this.timestamp = timestamp;
		this.path = path;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Map<String, String> getValidationErrors() {
		return validationErrors;
	}

	public void setValidationErrors(Map<String, String> validationErrors) {
		this.validationErrors = validationErrors;
	}
	
	
	
	
	
	
}
