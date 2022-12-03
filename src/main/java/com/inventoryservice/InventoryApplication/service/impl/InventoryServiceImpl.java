package com.inventoryservice.InventoryApplication.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.inventoryservice.InventoryApplication.entity.Product;
import com.inventoryservice.InventoryApplication.repository.InventoryRepository;
import com.inventoryservice.InventoryApplication.service.InventoryService;

@Service
public class InventoryServiceImpl implements InventoryService{

	private String adminToken="admin2410Gaur2410";
	private String buyerToken;
	
	@Autowired
	InventoryRepository inventoryRepository;
	
	@Override
	public String test() {
		
		return "Gauranga";
	}

	@Override
	public String generateToken(String userName, String password) {
		String token="";
		token+=userName;
		if(userName.equals("admin"))
			token+="2410";
		else token+="1024";
		token+=password;
		if(userName.equals("admin"))
			token+="2410";
		else token+="1024";
		
		if(userName.equals("admin")) return adminToken=token;
		else return buyerToken=token;
		
	}

	public Product addProduct(String token, Product product) {
		if(token.equals(adminToken)) {
			inventoryRepository.save(product);
			return product;
		}
		return null;
		
	}

	


	
	
}
 