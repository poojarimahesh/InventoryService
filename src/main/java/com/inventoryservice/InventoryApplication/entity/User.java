package com.inventoryservice.InventoryApplication.entity;

import java.util.Date;


import org.springframework.stereotype.Component;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Component
@Entity
public class User {
	@Id
	private String userName;
	private String password;
	
	public Date getCreatedTime() {
		return createdTime;
	}
	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}
	private String token;
	
	@Temporal(value=TemporalType.TIMESTAMP)
	@Column(name = "Created_Time")
	private Date createdTime;
	
	
	
	
	@Override
	public String toString() {
		return "User [userName=" + userName + ", password=" + password + ", token=" + token + ", createdTime="
				+ createdTime + "]";
	}
	public User(String userName, String password, String token, Date createdTime) {
		super();
		this.userName = userName;
		this.password = password;
		this.token = token;
		this.createdTime = createdTime;
	}
	public User() {
		super();
		// TODO Auto-generated constructor stub
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	
	
	
}
