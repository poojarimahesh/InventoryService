package com.inventoryservice.InventoryApplication.service.impl;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.el.util.ReflectionUtil;
import org.aspectj.lang.reflect.FieldSignature;
import org.aspectj.weaver.NewConstructorTypeMunger;
import org.hibernate.query.NativeQuery.ReturnableResultNode;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.util.ReflectionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.support.SimpleTriggerContext;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import com.inventoryservice.InventoryApplication.entity.Product;
import com.inventoryservice.InventoryApplication.entity.User;
import com.inventoryservice.InventoryApplication.error.NotAuthorizedException;
import com.inventoryservice.InventoryApplication.error.ProductNotFoundException;
import com.inventoryservice.InventoryApplication.error.TokenExpiredException;
import com.inventoryservice.InventoryApplication.repository.InventoryProductRepository;
import com.inventoryservice.InventoryApplication.repository.InventoryUserRepository;
import com.inventoryservice.InventoryApplication.service.InventoryService;

@Service
public class InventoryServiceImpl implements InventoryService{

	private static String adminToken="admin2410Gaur2410";
	private static String buyerToken="buyer1024mahesh1024";
	
	@Autowired
	InventoryProductRepository inventoryProductRepository;
	
	@Autowired
	InventoryUserRepository inventoryUserRepository;
	

	
	
	void setCreatedDateToInvalid(String userName) {
		Date storedDate =inventoryUserRepository.findById(userName).get().getCreatedTime();
		storedDate.setHours(storedDate.getHours()-2);
		User user = inventoryUserRepository.findById(userName).get();
		user.setCreatedTime(storedDate);
		inventoryUserRepository.save(user);
	}
	
	@Override
	public Boolean isTokenExpired(String userName) {
		Date storedDate =inventoryUserRepository.findById(userName).get().getCreatedTime();
		
		Date expiryDate = new Date();;
		expiryDate.setHours(storedDate.getHours()+1);
		expiryDate.setMinutes(storedDate.getMinutes());
		expiryDate.setSeconds(storedDate.getSeconds());
		expiryDate.setDate(storedDate.getDate());
		expiryDate.setMonth(storedDate.getMonth());
		expiryDate.setYear(storedDate.getYear());
//	 
//		System.out.println("Time stored in DB " + storedDate);
//		System.out.println("Expiry Time " + expiryDate);
		if(expiryDate.compareTo(new Date())>=0) {
//			System.out.println("Not Expired");
			return false;
		}
		
		else 
//			System.out.println("Expired");
		return true;
	}

	@Override
	public Boolean verifyAdminToken(String token) throws TokenExpiredException {
		if(this.isTokenExpired("admin")) throw new TokenExpiredException("Token is Expired, Please regenerate your token");
		return ((inventoryUserRepository.findById("admin").get().getToken().trim()).equals(token.trim()));

	}
	
	@Override
	public Boolean verifyBuyerToken(String token) throws TokenExpiredException {
		if(this.isTokenExpired("mahesh")) throw new TokenExpiredException("Token is Expired, Please regenerate your token");
		return ((inventoryUserRepository.findById("mahesh").get().getToken().trim()).equals(token.trim()));
	}

	@Override
	public ResponseEntity<String> generateToken(String userName, String password) throws NotAuthorizedException, TokenExpiredException {
			

		Optional<User > user = inventoryUserRepository.findById(userName.trim());
		if(!user.isPresent()) throw new NotAuthorizedException("Provide correct UserName");
		else if(!user.get().getPassword().trim().equals(password.trim()))throw new NotAuthorizedException("Provide correct Password ");
		else if(isTokenExpired(userName) || user.get().getToken()==null) {
			Date tempDate = new Date();
			DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-ddhh:mm:ss");  
			String strDate = dateFormat.format(tempDate);  
			String generatedtoken = DigestUtils.sha256Hex(userName+password+strDate);
			user.get().setToken(generatedtoken);			
			user.get().setCreatedTime(new Date());
			inventoryUserRepository.save(user.get());
			return ResponseEntity.status(HttpStatus.CREATED).body(generatedtoken);
		}
		else 
			return ResponseEntity.status(HttpStatus.ACCEPTED).body(inventoryUserRepository.findById(userName).get().getToken().trim());
	}
	
	
	@Override
	public ResponseEntity<String> addProduct(String token, Product product) throws NotAuthorizedException, TokenExpiredException {
		// if you want to decrease hours in DB then uncomment below line
//		setCreatedDateToInvalid("admin");
		if(!this.verifyAdminToken(token)) {
			throw new NotAuthorizedException("You are not Authorized , Please provide correct Token");
		}
		
		inventoryProductRepository.save(product);
		return ResponseEntity.status(HttpStatus.CREATED).body("Product added Successfully");

	}

	
	@Override
	public ResponseEntity<List<Product>> viewProducts(String token, String brandName,String price , String modelName) throws NotAuthorizedException, TokenExpiredException {
		if(!verifyAdminToken(token) && !verifyBuyerToken(token))
			throw new NotAuthorizedException("You are not Authorized , Please provide correct Token");
		List<Product> resultantListOfProduct= inventoryProductRepository.findAll();
		List<Product> resultList = new ArrayList<>();
		if(brandName!=null) {
			resultList= this.filterByBrandName(resultantListOfProduct, brandName);
			resultantListOfProduct.clear();
			resultantListOfProduct.addAll(resultList);
		}
		if(price!=null) {
			resultList.clear();
			resultList = this.filterByPrice(resultantListOfProduct, price);
			resultantListOfProduct.clear();
			resultantListOfProduct.addAll(resultList);
		}
		if(modelName!=null) {
			resultList.clear();
			resultList=this.filterByModelName(resultantListOfProduct, modelName);
			resultantListOfProduct.clear();
			resultantListOfProduct.addAll(resultList);
		}
		return ResponseEntity.status(HttpStatus.OK).body(resultantListOfProduct);
	}

	@Override
	public ResponseEntity<Product> updateProduct(Long productId,String token,Map<Object, Object> updatedFields) throws ProductNotFoundException, NotAuthorizedException, TokenExpiredException {
		if(!this.verifyAdminToken(token))
			throw new NotAuthorizedException("You are not Authorized Admin , Please provide correct Token");
		
		Optional<Product> optionalProduct = inventoryProductRepository.findById(productId);
		if(!optionalProduct.isPresent()) throw new ProductNotFoundException("Product Not available, Please provide valid productId");
		Product retreivedProduct=optionalProduct.get();
		updatedFields.forEach((key,value)-> {
			Field field =ReflectionUtils.findField(Product.class,(String) key);
			field.setAccessible(true);
			ReflectionUtils.setField(field, retreivedProduct, value);
		});
		
		inventoryProductRepository.save(retreivedProduct);
		
		return ResponseEntity.status(HttpStatus.OK).body(retreivedProduct);
		
	}

	@Override
	public ResponseEntity<String> deleteProduct(String token, String productId) throws ProductNotFoundException, NotAuthorizedException, TokenExpiredException {
		if(!this.verifyAdminToken(token))
			throw new NotAuthorizedException("You are not Authorized admin, Please provide correct Token");
		
		Optional<Product> optionalProduct =inventoryProductRepository.findById(Long.parseLong(productId));
		if(!optionalProduct.isPresent()) throw new ProductNotFoundException("Invalid Product ID for deleting Product");
		inventoryProductRepository.deleteById(Long.parseLong(productId));
		return ResponseEntity.status(HttpStatus.OK).body("Successfully Deleted the Product");
	}
	
	@Override
	public ResponseEntity<String> buyProduct(Long productId, String quantity, String token) throws NotAuthorizedException, ProductNotFoundException, TokenExpiredException {
		long quantityOfProduct =Long.parseLong(quantity);
		if(!this.verifyBuyerToken(token))
			throw new NotAuthorizedException("You are not Authorized to buy Product, Please provide valid Buyer's Token");
		if(quantityOfProduct>3)
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not allowed to request more than 3 quantity");
		
//		Using Product Name for buying product
//		List<Product> products = inventoryProductRepository.findAll();
//		for(Product product : products) {
//			System.out.println(product.getProductName());
//			System.out.println(name);
//			
//			if(product.getProductName().equals(name.trim())) {
//				if(product.getQuantity()>=quantityOfProduct) {
//					product.setQuantity(product.getQuantity()-quantityOfProduct);
//					inventoryProductRepository.save(product);
//					return ResponseEntity.status(HttpStatus.OK).body("Successfully Buyed Product and reduced the quantity in DB");
//				}else {
//					return ResponseEntity.status(HttpStatus.INSUFFICIENT_STORAGE).body("No Sufficient Quantity of Product");
//				}
//			}
//		}
		Optional<Product> optionalProduct = inventoryProductRepository.findById(productId);
		if(!optionalProduct.isPresent())
			throw new ProductNotFoundException("Product Not found, Please provide valid productId");
		else {
			Product product=optionalProduct.get();
			if(product.getQuantity()>=quantityOfProduct) {
				product.setQuantity(product.getQuantity()-quantityOfProduct);
				inventoryProductRepository.save(product);
				return ResponseEntity.status(HttpStatus.OK).body("Successfully Buyed Product and reduced the quantity in DB");
			}else {
				return ResponseEntity.status(HttpStatus.INSUFFICIENT_STORAGE).body("No Sufficient Quantity of Product");
			}
		} 
	}

	@Override
	public List<Product> filterByBrandName(List<Product> listOfProduct, String brandName) {
		List<Product> resultList = new ArrayList<>();
		for(Product tempProductIterator : listOfProduct) {
			if(tempProductIterator.getBrandName().trim().equals(brandName.trim())) resultList.add(tempProductIterator);
			
		}
		return resultList;
	}
	
	@Override
	public List<Product> filterByModelName(List<Product> listOfProduct, String modelName) {
		List<Product> resultList = new ArrayList<>();
		for(Product tempProductIterator : listOfProduct) {
			
			System.out.println(tempProductIterator.getModelName().equals(modelName));
			if(tempProductIterator.getModelName().trim().equals(modelName.trim())) resultList.add(tempProductIterator);
			
		}
		return resultList;
	}
	
	@Override
	public List<Product> filterByPrice(List<Product> listOfProduct, String price) {
		List<Product> resultList = new ArrayList<>();
		for(Product tempProductIterator : listOfProduct) {
			if(tempProductIterator.getPrice()<= Long.parseLong(price)) resultList.add(tempProductIterator);
			
		}
		return resultList;
	}

	
	

	


	
	
}
 