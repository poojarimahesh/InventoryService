package com.inventoryservice.InventoryApplication.error;

import org.springframework.http.HttpStatus;

public class NotAuthorizedError {
	private HttpStatus status;
	private String message;
	
	
	public HttpStatus getStatus() {
		return status;
	}
	public void setStatus(HttpStatus status) {
		this.status = status;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public NotAuthorizedError() {
		super();
		// TODO Auto-generated constructor stub
	}
	public NotAuthorizedError(HttpStatus status, String message) {
		super();
		this.status = status;
		this.message = message;
	}
	
	
}
