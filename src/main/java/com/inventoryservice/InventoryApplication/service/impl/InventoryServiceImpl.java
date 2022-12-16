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
import java.util.stream.Collector;
import java.util.stream.Collectors;

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
	
	@Autowired
	InventoryProductRepository inventoryProductRepository;
	
	@Autowired
	InventoryUserRepository inventoryUserRepository;
	

	
//	This method will help to make Token invalid 
	void setCreatedDateToInvalid(String userName) {
		Date storedDate =inventoryUserRepository.findById(userName).get().getCreatedTime();
		storedDate.setHours(storedDate.getHours()-2);
		User user = inventoryUserRepository.findById(userName).get();
		user.setCreatedTime(storedDate);
		inventoryUserRepository.save(user);
	}

//	this method will help to insert Users in DB
	void createAndAddUsersToDB() {
		User admin = new User("admin","InventoryAdmin",null,new Date());
		User buyer = new User("mahesh","InventoryMahesh",null,new Date());
		inventoryUserRepository.save(admin);
		inventoryUserRepository.save(buyer);
	}

	
//	This method will check whether the Token is Expired or not 
//	if expired then it will return true 
//	if not expired then it will return false
	@Override
	public Boolean isTokenExpired(String userName) {
		Date storedDate =inventoryUserRepository.findById(userName).get().getCreatedTime();
		
		Date expiryDate = new Date();
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

	
//	This method will verify token of admin
//	1 : It will check whether the token is Expired or not if yes it will throw TokenExpiredException
//	2 : It will check whether the passed token is equal to the stored token in DB if yes then it return true if not then return false
	@Override
	public Boolean verifyAdminToken(String token) throws TokenExpiredException {
		if(this.isTokenExpired("admin")) throw new TokenExpiredException("Token is Expired, Admin please regenerate your token");
		return ((inventoryUserRepository.findById("admin").get().getToken().trim()).equals(token.trim()));

	}
	
//	This method will verify token of Buyer
//	1 : It will check whether the token is Expired or not if yes it will throw TokenExpiredException
//	2 : It will check whether the passed token is equal to the stored token in DB if yes then it return true if not then return false	
	@Override
	public Boolean verifyBuyerToken(String token) throws TokenExpiredException {
		if(this.isTokenExpired("mahesh")) throw new TokenExpiredException("Token is Expired, Buyer please regenerate your token");
		return ((inventoryUserRepository.findById("mahesh").get().getToken().trim()).equals(token.trim()));
	}

//	This method will generate Token 
//	1 : Will check for whether the username provided is correct or not if incorrect then throw NotAuthorizedException
//	2 : Will check for whether the password provided is correct or not if incorrect then throw NotAuthorizedException
//	3 : if Token is expired then Generate Token and return the generated Token
//	4 : If the Token already exists in DB then return the stored Token 
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
	
//	This method will add Product in Inventory 
//	1 : Will verify provided Token whether it is of Admin if Not then throw NotAuthorizedException
//	2 : Save product in inventoryProductRepository
//	3 : return ResponseEntity with status as Created and Body as String "Product added Successfully"
	@Override
	public ResponseEntity<String> addProduct(String token, Product product) throws NotAuthorizedException, TokenExpiredException {
		// if you want to decrease hours in DB then uncomment below line
//		setCreatedDateToInvalid("admin");
//		setCreatedDateToInvalid("mahesh");
		if(!this.verifyAdminToken(token)) {
			throw new NotAuthorizedException("You are not Authorized Admin, Please provide correct Token");
		}
		
		inventoryProductRepository.save(product);
		return ResponseEntity.status(HttpStatus.CREATED).body("Product added Successfully");

	}

//	This method will return List of Products from Inventory DB 
//	1 : Will verify provided Token whether it is of Admin or Buyer if Not then throw NotAuthorizedException
//	2 : retrive all Products by calling inventoryProductRepository.findAll() 
//	3 : if brandName is not null then filter the list which only contains provided brandName
//	4 : if modelName is not null then filter the list which only contains provided modelName
//	5 : if price is not null then filter the list which only contains provided price
	@Override
	public ResponseEntity<List<Product>> viewProducts(String token, String brandName,String price , String modelName) throws NotAuthorizedException, TokenExpiredException {
		if(!verifyAdminToken(token) && !verifyBuyerToken(token))
			throw new NotAuthorizedException("You are not Authorized User, Please provide correct Token");
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

	
//	This method will return updated Product from Inventory DB 
//	1 : Will verify provided Token whether it is of Admin if Not then throw NotAuthorizedException
//	2 : Will verify provided ProductId whether it is valid productId stored in DB if Not then throw ProductNotFoundException 
//	3 : Update the provided fields in Local Variable
//	4 : Save the updated product in DB
//	5 : return the updated Product 

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

//	This method will return deleted Product from Inventory DB 
//	1 : Will verify provided Token whether it is of Admin if Not then throw NotAuthorizedException
//	2 : Will verify provided ProductId whether it is valid productId stored in DB if Not then throw ProductNotFoundException 
//	3 : Deleted the product from inventoryProductRepository i.e DB
//	4 : return the deleted Product 
	@Override
	public ResponseEntity<String> deleteProduct(String token, String productId) throws ProductNotFoundException, NotAuthorizedException, TokenExpiredException {
		if(!this.verifyAdminToken(token))
			throw new NotAuthorizedException("You are not Authorized Admin, Please provide correct Token");
		
		Optional<Product> optionalProduct =inventoryProductRepository.findById(Long.parseLong(productId));
		if(!optionalProduct.isPresent()) throw new ProductNotFoundException("Invalid Product ID for deleting Product");
		inventoryProductRepository.deleteById(Long.parseLong(productId));
		return ResponseEntity.status(HttpStatus.OK).body("Successfully Deleted the Product");
	}
	
	
//	This method will help to buy Product from Inventory DB 
//	1 : Will verify provided Token whether it is of Admin if Not then throw NotAuthorizedException
//	2 : if required quantity is more than 3 then return message indicating it is not allowed to request more than 3 
//	2 : Will verify provided ProductId whether it is valid productId stored in DB if Not then throw ProductNotFoundException 
//	3 : If the stored quantity is less than the required quantity then return error message indicating Insufficient Quantity
//	4 : Update the quantity in DB
//	5 : Return the message indicating the Successfully buyed the product and updated the quantity in DB
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
	
//	This method will filter based on BrandName
//	1 : Creates a temporary List and store all the Products having brandName same as the passed brandName
//	2 : Return the temporaryList which stores all filtered Product List

	@Override
	public List<Product> filterByBrandName(List<Product> listOfProduct, String brandName) {
		List<Product> resultList = new ArrayList<>();
		resultList=listOfProduct.stream().filter(productIterator -> (productIterator.getBrandName().trim().equals(brandName.trim()))).collect(Collectors.toList());
		return resultList;
	}
	
//	This method will filter based on ModelName
//	1 : Creates a temporary List and store all the Products having modelName same as the passed modelName
//	2 : Return the temporaryList which stores all filtered Product List

	@Override
	public List<Product> filterByModelName(List<Product> listOfProduct, String modelName) {
		List<Product> resultList = new ArrayList<>();
		resultList=listOfProduct.stream().filter(productIterator -> (productIterator.getModelName().trim().equals(modelName.trim()))).collect(Collectors.toList());
		return resultList;
	}
	
//	This method will filter based on price
//	1 : Creates a temporary List and store all the Products having price same as the passed price
//	2 : Return the temporaryList which stores all filtered Product List
	
	@Override
	public List<Product> filterByPrice(List<Product> listOfProduct, String price) {
		List<Product> resultList = new ArrayList<>();
		resultList=listOfProduct.stream().filter(productIterator -> (productIterator.getPrice()<= Long.parseLong(price))).collect(Collectors.toList());
		return resultList;
	}

	
	

	


	
	
}
 