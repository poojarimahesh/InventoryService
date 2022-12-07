package com.inventoryservice.InventoryApplication.service;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.inventoryservice.InventoryApplication.entity.Product;
import com.inventoryservice.InventoryApplication.error.NotAuthorizedException;
import com.inventoryservice.InventoryApplication.error.ProductNotFoundException;
import com.inventoryservice.InventoryApplication.error.TokenExpiredException;

public interface InventoryService {
	Boolean verifyAdminToken(String token) throws TokenExpiredException;
	ResponseEntity<String> generateToken(String UserName, String Password) throws NotAuthorizedException, TokenExpiredException;
	ResponseEntity<String> addProduct(String token,Product product) throws  NotAuthorizedException, TokenExpiredException;
	ResponseEntity<List<Product>> viewProducts(String token ,String brandName,String price , String modelName) throws NotAuthorizedException, TokenExpiredException;
	ResponseEntity<Product> updateProduct(Long productId,String token,Map<Object, Object> updatedFields) throws ProductNotFoundException, NotAuthorizedException, TokenExpiredException;
	ResponseEntity<String> deleteProduct(String token, String productId) throws ProductNotFoundException, NotAuthorizedException, TokenExpiredException;
	ResponseEntity<String> buyProduct(Long productId, String quantity, String token) throws NotAuthorizedException, ProductNotFoundException, TokenExpiredException;
	Boolean verifyBuyerToken(String token) throws TokenExpiredException;
	List<Product> filterByBrandName(List<Product> listOfProduct, String brandName);
	List<Product> filterByModelName(List<Product> listOfProduct, String modelName);
	List<Product> filterByPrice(List<Product> listOfProduct, String price);
	Boolean isTokenExpired(String userName);
	
	
}
