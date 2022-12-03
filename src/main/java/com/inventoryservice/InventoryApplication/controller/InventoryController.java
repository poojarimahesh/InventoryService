package com.inventoryservice.InventoryApplication.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.inventoryservice.InventoryApplication.entity.Product;
import com.inventoryservice.InventoryApplication.service.impl.InventoryServiceImpl;

@RestController
public class InventoryController {
	
	
	private InventoryServiceImpl inventoryServiceImpl;
	
	@Autowired
	public InventoryController(InventoryServiceImpl inventoryServiceImpl) {
		super();
		this.inventoryServiceImpl = inventoryServiceImpl;
	}

	@GetMapping("/getToken")
	public String test(@RequestParam String username ,@RequestParam String password) {
		
		return inventoryServiceImpl.generateToken(username,password);
	}
	
	@PostMapping("/addProduct")
	public Product addProduct(@RequestParam String token, @RequestBody Product product) {
		return inventoryServiceImpl.addProduct(token,product);
	}
	
	
}
