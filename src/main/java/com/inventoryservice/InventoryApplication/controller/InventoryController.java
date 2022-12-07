package com.inventoryservice.InventoryApplication.controller;


import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.inventoryservice.InventoryApplication.entity.Product;
import com.inventoryservice.InventoryApplication.error.NotAuthorizedException;
import com.inventoryservice.InventoryApplication.error.ProductNotFoundException;
import com.inventoryservice.InventoryApplication.error.TokenExpiredException;
import com.inventoryservice.InventoryApplication.service.impl.InventoryServiceImpl;

import jakarta.validation.Valid;

@RestController
public class InventoryController {
	
	
	private InventoryServiceImpl inventoryServiceImpl;
	
	@Autowired
	public InventoryController(InventoryServiceImpl inventoryServiceImpl) {
		super();
		this.inventoryServiceImpl = inventoryServiceImpl;
	}

	@GetMapping("/getToken")
	public ResponseEntity<String> test(@RequestParam String username ,@RequestParam String password) throws NotAuthorizedException, TokenExpiredException {
		
		return inventoryServiceImpl.generateToken(username,password);
	}
	
	@PostMapping("/addProduct")
	public ResponseEntity<String> addProduct(@RequestParam String token, @RequestBody Product product) throws  NotAuthorizedException, TokenExpiredException {
		return inventoryServiceImpl.addProduct(token,product);
	}
	
	@GetMapping("/viewProducts")
	public ResponseEntity<List<Product>> viewProducts(@RequestParam String token, @RequestParam(required = false) String brandName,@RequestParam(required = false) String price,@RequestParam(required = false) String modelName) throws NotAuthorizedException, TokenExpiredException{
		return inventoryServiceImpl.viewProducts(token,brandName,price,modelName);
	}
	
	@PatchMapping("/updateProduct")
	public ResponseEntity<Product> updateProduct(@RequestParam Long productId,@RequestParam String token ,@Valid  @RequestBody Map<Object,Object> updatedFields) throws ProductNotFoundException, NotAuthorizedException, TokenExpiredException {
		return inventoryServiceImpl.updateProduct(productId,token, updatedFields);
	}
	
	@DeleteMapping("/deleteProduct")
	public ResponseEntity<String> deleteProduct(@RequestParam String token , @RequestParam String productId) throws ProductNotFoundException, NotAuthorizedException, TokenExpiredException {
		return inventoryServiceImpl.deleteProduct(token,productId);
	}
	
	@GetMapping("/buyProduct")
	public ResponseEntity<String> buyProduct(@RequestParam Long productId, @RequestParam String quantity,@RequestParam String token) throws NotAuthorizedException, ProductNotFoundException, TokenExpiredException {
		return inventoryServiceImpl.buyProduct(productId, quantity,token);
	}
}
