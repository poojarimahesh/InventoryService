package com.inventoryservice.InventoryApplication.service;

import com.inventoryservice.InventoryApplication.entity.Product;

public interface InventoryService {
	String test();
	String generateToken(String UserName, String Password);
//	Product addProduct(String token,Product product);
}
