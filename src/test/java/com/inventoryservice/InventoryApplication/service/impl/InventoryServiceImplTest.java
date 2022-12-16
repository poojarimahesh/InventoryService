package com.inventoryservice.InventoryApplication.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException.Unauthorized;

import com.inventoryservice.InventoryApplication.entity.Product;
import com.inventoryservice.InventoryApplication.entity.User;
import com.inventoryservice.InventoryApplication.error.NotAuthorizedException;
import com.inventoryservice.InventoryApplication.error.ProductNotFoundException;
import com.inventoryservice.InventoryApplication.error.TokenExpiredException;
import com.inventoryservice.InventoryApplication.repository.InventoryProductRepository;
import com.inventoryservice.InventoryApplication.repository.InventoryUserRepository;
import com.mysql.cj.protocol.x.Ok;

@SpringBootTest
class InventoryServiceImplTest {

	@MockBean
	InventoryProductRepository inventoryProductRepository;
	

	@MockBean
	InventoryUserRepository inventoryUserRepository;
	
	@Autowired
	InventoryServiceImpl inventoryServiceImpl;
	
	
	@Test
	@DisplayName("VerifyAdminToken_TokenExpiredException")
	void whenExpiredAdminTokenInVerifyAdminToken_thenReturnTokenExpiredException() throws NotAuthorizedException, TokenExpiredException{

		Date date = new Date();
		date.setHours(date.getHours()-2);
		Optional<User> user = Optional.of(new User("admin","InventoryAdmin","94ac92acb7cf09b4da78d99de9368e9256c625dc974b688532762919807c58bbb",date));
		Mockito.when(inventoryUserRepository.findById("admin")).thenReturn(user);
		assertThrows(TokenExpiredException.class, () -> inventoryServiceImpl.verifyAdminToken("94ac92acb7cf09b4da78d99de9368e9256c625dc974b688532762919807c58bbb"));
		
	}
	
	@Test
	@DisplayName("VerifyAdminToken_InvalidAdminToken")
	void whenInvalidAdminTokenInVerifyAdminToken_thenReturnTokenExpiredException() throws NotAuthorizedException, TokenExpiredException{

		Optional<User> user = Optional.of(new User("admin","InventoryAdmin","94ac92acb7cf09b4da78d99de9368e9256c625dc974b688532762919807c58bbb",new Date()));
		Mockito.when(inventoryUserRepository.findById("admin")).thenReturn(user);
		assertFalse(inventoryServiceImpl.verifyAdminToken("94ac92acb7cf09b4da78d99de9368e9256c625dc974b688532762919807c58b"));
	}
	
	@Test
	@DisplayName("VerifyAdminToken_Successful")
	void whenValidAdminTokenInVerifyAdminToken_thenReturnTrue() throws  TokenExpiredException{

		Optional<User> user = Optional.of(new User("admin","InventoryAdmin","94ac92acb7cf09b4da78d99de9368e9256c625dc974b688532762919807c58bbb",new Date()));
		Mockito.when(inventoryUserRepository.findById("admin")).thenReturn(user);
		assertTrue(inventoryServiceImpl.verifyAdminToken("94ac92acb7cf09b4da78d99de9368e9256c625dc974b688532762919807c58bbb"));
		
	}
	
	@Test
	@DisplayName("VerifyBuyerToken_TokenExpiredException")
	void whenExpiredBuyerTokenInVerifyBuyerToken_thenReturnTokenExpiredException() throws TokenExpiredException{

		Date date = new Date();
		date.setHours(date.getHours()-2);
		Optional<User> user = Optional.of(new User("mahesh","InventoryBuyer","8ef09efc7c34c02e79611e50c722a042759970c4d0fe8e1ec7e9e48501daf788",date));
		Mockito.when(inventoryUserRepository.findById("mahesh")).thenReturn(user);
		assertThrows(TokenExpiredException.class, () -> inventoryServiceImpl.verifyBuyerToken("8ef09efc7c34c02e79611e50c722a042759970c4d0fe8e1ec7e9e48501daf788"));
		
	}
	
	
	@Test
	@DisplayName("VerifyBuyerToken_Successful")
	void whenValidBuyerTokenInVerifyBuyerToken_thenReturnTrue() throws TokenExpiredException{

		Optional<User> user = Optional.of(new User("mahesh","InventoryBuyer","8ef09efc7c34c02e79611e50c722a042759970c4d0fe8e1ec7e9e48501daf788",new Date()));
		Mockito.when(inventoryUserRepository.findById("mahesh")).thenReturn(user);
		String expectedBuyerToken = "8ef09efc7c34c02e79611e50c722a042759970c4d0fe8e1ec7e9e48501daf788";
		assertTrue(inventoryServiceImpl.verifyBuyerToken("8ef09efc7c34c02e79611e50c722a042759970c4d0fe8e1ec7e9e48501daf788"));
		
	}
	
	@Test
	@DisplayName("VerifyBuyerToken_InvalidBuyerToken")
	void whenInValidBuyerTokenInVerifyBuyerToken_thenReturnTrue() throws TokenExpiredException{

		Optional<User> user = Optional.of(new User("mahesh","InventoryBuyer","8ef09efc7c34c02e79611e50c722a042759970c4d0fe8e1ec7e9e48501daf788",new Date()));
		Mockito.when(inventoryUserRepository.findById("mahesh")).thenReturn(user);
		String expectedBuyerToken = "8ef09efc7c34c02e79611e50c722a042759970c4d0fe8e1ec7e9e48501daf788";
		assertFalse(inventoryServiceImpl.verifyBuyerToken("8ef09efc7c34c02e79611e50c722a042759970c4d0fe8e1ec7e9e48501daf7"));
		
	}
	
	@Test
	@DisplayName("GenerateToken Successful")
	void whenAllValidInGenerateToken_thenReturnCreatedToken() throws NotAuthorizedException, TokenExpiredException{

		Optional<User> user = Optional.of(new User("admin","InventoryAdmin",null,new Date()));
		Mockito.when(inventoryUserRepository.findById("admin")).thenReturn(user);
		assertEquals(HttpStatus.CREATED, inventoryServiceImpl.generateToken("admin","InventoryAdmin").getStatusCode());
		
	}
	
	@Test
	@DisplayName("GenerateToken_InvalidUserName")
	void whenInValidUserNameInGenerateToken_thenReturnNotAuthorizedException() throws NotAuthorizedException, TokenExpiredException{

		Optional<User> user = Optional.of(new User("admin","InventoryAdmin",null,new Date()));
		Mockito.when(inventoryUserRepository.findById("admin")).thenReturn(user);
		assertThrows(NotAuthorizedException.class, () -> inventoryServiceImpl.generateToken("admi", "InventoryAdmin"));
		
	}
	
	@Test
	@DisplayName("GenerateToken_InvalidPassword")
	void whenInValidPasswordInGenerateToken_thenReturnNotAuthorizedException() throws NotAuthorizedException, TokenExpiredException{

		Optional<User> user = Optional.of(new User("admin","InventoryAdmin","94ac92acb7cf09b4da78d99de9368e9256c625dc974b688532762919807c58bbb",new Date()));
		Mockito.when(inventoryUserRepository.findById("admin")).thenReturn(user);
		assertThrows(NotAuthorizedException.class,() -> inventoryServiceImpl.generateToken("admin","InventoryAdmi"));
		
	}
	
	@Test
	@DisplayName("GenerateToken AlreadyCreatedToken")
	void whenAlreadyGeneratedTokenInGenerateToken_thenReturnStoredToken() throws NotAuthorizedException, TokenExpiredException{

		Optional<User> user = Optional.of(new User("admin","InventoryAdmin","94ac92acb7cf09b4da78d99de9368e9256c625dc974b688532762919807c58bbb",new Date()));
		Mockito.when(inventoryUserRepository.findById("admin")).thenReturn(user);
		ResponseEntity<String> expectedResponseEntity = new ResponseEntity<String>("94ac92acb7cf09b4da78d99de9368e9256c625dc974b688532762919807c58bbb",HttpStatus.ACCEPTED);
		assertEquals(expectedResponseEntity, inventoryServiceImpl.generateToken("admin","InventoryAdmin"));
		
	}
	
	@Test
	@DisplayName("AddProduct Successful")
	void whenAllValid_thenReturnOK() throws NotAuthorizedException, TokenExpiredException {
		Product product = new Product();
		product.setBrandName("Codeverse");
		product.setModelName("This course will help you to understand features of Spring boot");
		product.setPrice(30000);
		product.setProductId(301);
		Optional<User> user = Optional.of(new User("admin","InventoryAdmin","94ac92acb7cf09b4da78d99de9368e9256c625dc974b688532762919807c58bbb",new Date()));
		Mockito.when(inventoryUserRepository.findById("admin")).thenReturn(user);
		Mockito.when(inventoryProductRepository.save(product)).thenReturn(product);
		ResponseEntity<String> expectedEntity = new ResponseEntity<String>("Product added Successfully",HttpStatus.CREATED);
		assertEquals(expectedEntity, inventoryServiceImpl.addProduct("94ac92acb7cf09b4da78d99de9368e9256c625dc974b688532762919807c58bbb",product));
		Mockito.verify(inventoryProductRepository,times(1)).save(product);
		
	}
	
	@Test
	@DisplayName("AddProduct_Invalidtoken_UnAuthorizedException")
	void whenInvalidToken_inAddProduct_thenReturnUnAuthorizedException() throws NotAuthorizedException, TokenExpiredException {
		Product product = new Product();
		product.setBrandName("Codeverse");
		product.setModelName("This course will help you to understand features of Spring boot");
		product.setPrice(30000);
		product.setProductId(301);
		Optional<User> user = Optional.of(new User("admin","InventoryAdmin","94ac92acb7cf09b4da78d99de9368e9256c625dc974b688532762919807c58bbb",new Date()));
		Mockito.when(inventoryUserRepository.findById("admin")).thenReturn(user);
		assertThrows(NotAuthorizedException.class,() -> inventoryServiceImpl.addProduct("94ac92acb7cf09b4da78d99de9368e9256c625dc974b688532762919807c58b",product));
	
	}
	
	@Test
	@DisplayName("ViewProduct_InvalidBuyertoken_UnAuthorizedException")
	void whenInvalidBuyerTokenInViewProduct_thenReturnUnAuthorizedException() throws NotAuthorizedException, TokenExpiredException {
		List<Product> productList = new ArrayList<>();
		productList.add(new Product(301,"Spring boot","This course will help you to understand features of Spring boot","Codeverse",30000,50));
		productList.add(new Product(302,"Spring MVC","This course will help you to understand features of Spring ","Coding Ninjas",40000,60));
		productList.add(new Product(303,"Spring Data JPA","This course will help you to understand features of Spring ","Coding Ninjas",50000,50));
		productList.add(new Product(304,"Spring security","This course will help you to understand features of Spring security","Codeverse",35000,50));
		productList.add(new Product(305,"Spring","This course will help you to understand features of Spring","Codeverse",10000,50));
		
		Optional<User> admin = Optional.of(new User("admin","InventoryAdmin","94ac92acb7cf09b4da78d99de9368e9256c625dc974b688532762919807c58bbb",new Date()));
		Mockito.when(inventoryUserRepository.findById("admin")).thenReturn(admin);
		
		Optional<User> buyer = Optional.of(new User("mahesh","InventoryBuyer","8ef09efc7c34c02e79611e50c722a042759970c4d0fe8e1ec7e9e48501daf788",new Date()));
		Mockito.when(inventoryUserRepository.findById("mahesh")).thenReturn(buyer);
		
		Mockito.when(inventoryProductRepository.findAll()).thenReturn(productList);

		assertThrows(NotAuthorizedException.class,() -> inventoryServiceImpl.viewProducts("8ef09efc7c34c02e79611e50c722a042759970c4d0fe8e1ec7e9e48501daf7","Coding Ninjas","50000","This course will help you to understand features of Spring "));
	}
	
	
	@Test
	@DisplayName("ViewProduct_InvalidAdmintoken_UnAuthorizedException")
	void whenInvalidAdminTokenInViewProduct_thenReturnUnAuthorizedException() throws NotAuthorizedException, TokenExpiredException {
		List<Product> productList = new ArrayList<>();
		productList.add(new Product(301,"Spring boot","This course will help you to understand features of Spring boot","Codeverse",30000,50));
		productList.add(new Product(302,"Spring MVC","This course will help you to understand features of Spring ","Coding Ninjas",40000,60));
		productList.add(new Product(303,"Spring Data JPA","This course will help you to understand features of Spring ","Coding Ninjas",50000,50));
		productList.add(new Product(304,"Spring security","This course will help you to understand features of Spring security","Codeverse",35000,50));
		productList.add(new Product(305,"Spring","This course will help you to understand features of Spring","Codeverse",10000,50));
		
		Optional<User> admin = Optional.of(new User("admin","InventoryAdmin","94ac92acb7cf09b4da78d99de9368e9256c625dc974b688532762919807c58bbb",new Date()));
		Mockito.when(inventoryUserRepository.findById("admin")).thenReturn(admin);
		
		Optional<User> buyer = Optional.of(new User("mahesh","InventoryBuyer","8ef09efc7c34c02e79611e50c722a042759970c4d0fe8e1ec7e9e48501daf788",new Date()));
		Mockito.when(inventoryUserRepository.findById("mahesh")).thenReturn(buyer);
		
		Mockito.when(inventoryProductRepository.findAll()).thenReturn(productList);
		assertThrows(NotAuthorizedException.class,() -> inventoryServiceImpl.viewProducts("94ac92acb7cf09b4da78d99de9368e9256c625dc974b688532762919807c58b","Coding Ninjas","50000","This course will help you to understand features of Spring "));
	
	}
	
	@Test
	@DisplayName("ViewProduct_ValidAdmintoken_ListOfproducts")
	void whenValidAdminTokenInViewProduct_thenReturnListOfProducts() throws NotAuthorizedException, TokenExpiredException {
		List<Product> productList = new ArrayList<>();
		productList.add(new Product(301,"Spring boot","This course will help you to understand features of Spring boot","Codeverse",30000,50));
		productList.add(new Product(302,"Spring MVC","This course will help you to understand features of Spring ","Coding Ninjas",40000,60));
		productList.add(new Product(303,"Spring Data JPA","This course will help you to understand features of Spring ","Coding Ninjas",50000,50));
		productList.add(new Product(304,"Spring security","This course will help you to understand features of Spring security","Codeverse",35000,50));
		productList.add(new Product(305,"Spring","This course will help you to understand features of Spring","Codeverse",10000,50));
		
		Optional<User> admin = Optional.of(new User("admin","InventoryAdmin","94ac92acb7cf09b4da78d99de9368e9256c625dc974b688532762919807c58bbb",new Date()));
		Mockito.when(inventoryUserRepository.findById("admin")).thenReturn(admin);
		
		Optional<User> buyer = Optional.of(new User("mahesh","InventoryBuyer","8ef09efc7c34c02e79611e50c722a042759970c4d0fe8e1ec7e9e48501daf788",new Date()));
		Mockito.when(inventoryUserRepository.findById("mahesh")).thenReturn(buyer);
		
		Mockito.when(inventoryProductRepository.findAll()).thenReturn(productList);
//		assertThrows(NotAuthorizedException.class,() -> inventoryServiceImpl.viewProducts("94ac92acb7cf09b4da78d99de9368e9256c625dc974b688532762919807c58b","Coding Ninjas","50000","This course will help you to understand features of Spring "));
		List<Product> expectedListOfProducts = new ArrayList<>();
		expectedListOfProducts.add(new Product(302,"Spring MVC","This course will help you to understand features of Spring ","Coding Ninjas",40000,60));
		expectedListOfProducts.add(new Product(303,"Spring Data JPA","This course will help you to understand features of Spring ","Coding Ninjas",50000,50));
		
		ResponseEntity<List<Product>>  expectedResult =	new ResponseEntity<List<Product>>(expectedListOfProducts,HttpStatus.OK);
		
		assertEquals(expectedResult.toString(),inventoryServiceImpl.viewProducts("94ac92acb7cf09b4da78d99de9368e9256c625dc974b688532762919807c58bbb","Coding Ninjas","50000","This course will help you to understand features of Spring ").toString());
	}
	
	@Test
	@DisplayName("ViewProduct_ValidBuyertoken_ListOfproducts")
	void whenValidBuyerTokenInViewProduct_thenReturnListOfProducts() throws NotAuthorizedException, TokenExpiredException {
		List<Product> productList = new ArrayList<>();
		productList.add(new Product(301,"Spring boot","This course will help you to understand features of Spring boot","Codeverse",30000,50));
		productList.add(new Product(302,"Spring MVC","This course will help you to understand features of Spring ","Coding Ninjas",40000,60));
		productList.add(new Product(303,"Spring Data JPA","This course will help you to understand features of Spring ","Coding Ninjas",50000,50));
		productList.add(new Product(304,"Spring security","This course will help you to understand features of Spring security","Codeverse",35000,50));
		productList.add(new Product(305,"Spring","This course will help you to understand features of Spring","Codeverse",10000,50));
		
		Optional<User> admin = Optional.of(new User("admin","InventoryAdmin","94ac92acb7cf09b4da78d99de9368e9256c625dc974b688532762919807c58bbb",new Date()));
		Mockito.when(inventoryUserRepository.findById("admin")).thenReturn(admin);
		
		Optional<User> buyer = Optional.of(new User("mahesh","InventoryBuyer","8ef09efc7c34c02e79611e50c722a042759970c4d0fe8e1ec7e9e48501daf788",new Date()));
		Mockito.when(inventoryUserRepository.findById("mahesh")).thenReturn(buyer);
		
		Mockito.when(inventoryProductRepository.findAll()).thenReturn(productList);
		List<Product> expectedListOfProducts = new ArrayList<>();
		expectedListOfProducts.add(new Product(302,"Spring MVC","This course will help you to understand features of Spring ","Coding Ninjas",40000,60));
		expectedListOfProducts.add(new Product(303,"Spring Data JPA","This course will help you to understand features of Spring ","Coding Ninjas",50000,50));
		
		ResponseEntity<List<Product>>  expectedResult =	new ResponseEntity<List<Product>>(expectedListOfProducts,HttpStatus.OK);
		
		assertEquals(expectedResult.toString(),inventoryServiceImpl.viewProducts("8ef09efc7c34c02e79611e50c722a042759970c4d0fe8e1ec7e9e48501daf788","Coding Ninjas","50000","This course will help you to understand features of Spring ").toString());
	}
	
	@Test
	@DisplayName("UpdateProduct_InvalidAdmintoken_UnAuthorizedException")
	void whenInvalidAdminTokenInViewProduct_thenReturnUnauthorizedException() throws NotAuthorizedException, TokenExpiredException {
		
		Optional<Product> product =Optional.of(new Product(301,"Spring boot","This course will help you to understand features of Spring boot","Codeverse",30000,50));
		
		Optional<User> admin = Optional.of(new User("admin","InventoryAdmin","94ac92acb7cf09b4da78d99de9368e9256c625dc974b688532762919807c58bbb",new Date()));
		Mockito.when(inventoryUserRepository.findById("admin")).thenReturn(admin);
		
		Mockito.when(inventoryProductRepository.findById((long) 301)).thenReturn(product);
		Map<Object, Object> updatedFields = new HashMap<Object,Object>();
		updatedFields.put("productName", "Spring");
		updatedFields.put("brandName", "Coding Ninjas");
		updatedFields.put("quantity", 60);
		assertThrows(NotAuthorizedException.class, ()-> inventoryServiceImpl.updateProduct(301L, "94ac92acb7cf09b4da78d99de9368e9256c625dc974b688532762919807c58b", updatedFields));
		
	}
	
	@Test
	@DisplayName("UpdateProduct_InvalidProductId_ProductNotFoundException")
	void whenInvalidProductIdInUpdateProduct_thenReturnProductNotFoundException() throws NotAuthorizedException, TokenExpiredException {
		
		Optional<Product> product =Optional.of(new Product(301,"Spring boot","This course will help you to understand features of Spring boot","Codeverse",30000,50));
		
		Optional<User> admin = Optional.of(new User("admin","InventoryAdmin","94ac92acb7cf09b4da78d99de9368e9256c625dc974b688532762919807c58bbb",new Date()));
		Mockito.when(inventoryUserRepository.findById("admin")).thenReturn(admin);
		
		Mockito.when(inventoryProductRepository.findById((long) 301)).thenReturn(product);
		Map<Object, Object> updatedFields = new HashMap<Object,Object>();
		updatedFields.put("productName", "Spring");
		updatedFields.put("brandName", "Coding Ninjas");
		updatedFields.put("quantity", 60);
		assertThrows(ProductNotFoundException.class, ()-> inventoryServiceImpl.updateProduct(103L, "94ac92acb7cf09b4da78d99de9368e9256c625dc974b688532762919807c58bbb", updatedFields));
		
	}

	@Test
	@DisplayName("UpdateProduct_ValidInputs_ProductUpdatedSuccessful")
	void whenValidInputsInUpdateProduct_thenReturnProductUpdatedSuccessful() throws NotAuthorizedException, TokenExpiredException, ProductNotFoundException {
		
		Optional<Product> product =Optional.of(new Product(301,"Spring boot","This course will help you to understand features of Spring boot","Codeverse",30000,50));
		Optional<User> admin = Optional.of(new User("admin","InventoryAdmin","94ac92acb7cf09b4da78d99de9368e9256c625dc974b688532762919807c58bbb",new Date()));
		Mockito.when(inventoryUserRepository.findById("admin")).thenReturn(admin);
		Mockito.when(inventoryProductRepository.findById((long) 301)).thenReturn(product);
		Map<Object, Object> updatedFields = new HashMap<Object,Object>();
		updatedFields.put("productName", "Spring");
		updatedFields.put("brandName", "Coding Ninjas");
		updatedFields.put("quantity", 60);
		updatedFields.put("modelName", "This course will help you to understand features of Spring");
		Product expectedProduct = new Product(301,"Spring","This course will help you to understand features of Spring","Coding Ninjas",30000,60);
		ResponseEntity<Product> expectedResponse= new ResponseEntity<Product>(expectedProduct,HttpStatus.OK);
		assertEquals(expectedResponse.toString(), inventoryServiceImpl.updateProduct(301L, "94ac92acb7cf09b4da78d99de9368e9256c625dc974b688532762919807c58bbb", updatedFields).toString());
	}
	
	@Test
	@DisplayName("DeleteProduct_ValidInputs_ProductDeletedSuccessful")
	void whenValidInputsInDeleteProduct_thenReturnProductDeletedSuccessful() throws NotAuthorizedException, TokenExpiredException, ProductNotFoundException {
		
		Optional<Product> product =Optional.of(new Product(301,"Spring boot","This course will help you to understand features of Spring boot","Codeverse",30000,50));
		
		Optional<User> admin = Optional.of(new User("admin","InventoryAdmin","94ac92acb7cf09b4da78d99de9368e9256c625dc974b688532762919807c58bbb",new Date()));
		Mockito.when(inventoryUserRepository.findById("admin")).thenReturn(admin);
		
		Mockito.when(inventoryProductRepository.findById((long) 301)).thenReturn(product);

		ResponseEntity<String> expectedResponse= new ResponseEntity<String>("Successfully Deleted the Product",HttpStatus.OK);
		assertEquals(expectedResponse, inventoryServiceImpl.deleteProduct("94ac92acb7cf09b4da78d99de9368e9256c625dc974b688532762919807c58bbb", "301"));
	}
	
	@Test
	@DisplayName("DeleteProduct_InValidAdminToken_NotAuthorizedException")
	void whenInvalidAdminTokenInDeleteProduct_thenReturnNotAuthorizedException() throws NotAuthorizedException, TokenExpiredException, ProductNotFoundException {
		
		Optional<Product> product =Optional.of(new Product(301,"Spring boot","This course will help you to understand features of Spring boot","Codeverse",30000,50));
		
		Optional<User> admin = Optional.of(new User("admin","InventoryAdmin","94ac92acb7cf09b4da78d99de9368e9256c625dc974b688532762919807c58bbb",new Date()));
		Mockito.when(inventoryUserRepository.findById("admin")).thenReturn(admin);
		
		Mockito.when(inventoryProductRepository.findById((long) 301)).thenReturn(product);
		assertThrows(NotAuthorizedException.class, () -> inventoryServiceImpl.deleteProduct("94ac92acb7cf09b4da78d99de9368e9256c625dc974b688532762919807c58b", "301"));
	}
	
	@Test
	@DisplayName("DeleteProduct_InValidProductId_ProductNotFoundException")
	void whenInvalidProductIdInDeleteProduct_thenReturnProductNotFoundException() throws NotAuthorizedException, TokenExpiredException, ProductNotFoundException {
		
		Optional<Product> product =Optional.of(new Product(301,"Spring boot","This course will help you to understand features of Spring boot","Codeverse",30000,50));
		
		Optional<User> admin = Optional.of(new User("admin","InventoryAdmin","94ac92acb7cf09b4da78d99de9368e9256c625dc974b688532762919807c58bbb",new Date()));
		Mockito.when(inventoryUserRepository.findById("admin")).thenReturn(admin);
		
		Mockito.when(inventoryProductRepository.findById((long) 301)).thenReturn(product);
		assertThrows(ProductNotFoundException.class, () -> inventoryServiceImpl.deleteProduct("94ac92acb7cf09b4da78d99de9368e9256c625dc974b688532762919807c58bbb", "302"));
	}
	
	@Test
	@DisplayName("BuyProduct_ValidInput_SuccessfullMessage")
	void whenValidInputsInBuyProduct_thenReturnSuccessfulMessage() throws NotAuthorizedException, TokenExpiredException, ProductNotFoundException {
		
		Optional<Product> product =Optional.of(new Product(301,"Spring boot","This course will help you to understand features of Spring boot","Codeverse",30000,50));
		
		Optional<User> buyer = Optional.of(new User("mahesh","InventoryBuyer","8ef09efc7c34c02e79611e50c722a042759970c4d0fe8e1ec7e9e48501daf788",new Date()));
		Mockito.when(inventoryUserRepository.findById("mahesh")).thenReturn(buyer);
		
		Mockito.when(inventoryProductRepository.findById((long) 301)).thenReturn(product);
		ResponseEntity<String> expectedResponseEntity = new ResponseEntity<String>("Successfully Buyed Product and reduced the quantity in DB",HttpStatus.OK) ;
		assertEquals(expectedResponseEntity, inventoryServiceImpl.buyProduct(301L, "3", "8ef09efc7c34c02e79611e50c722a042759970c4d0fe8e1ec7e9e48501daf788"));
	}
	
	@Test
	@DisplayName("BuyProduct_InValidProductId_ProductNotFoundException")
	void whenInValidProductIdInBuyProduct_thenReturnProductNotFoundException() throws NotAuthorizedException, TokenExpiredException, ProductNotFoundException {
		
		Optional<Product> product =Optional.of(new Product(301,"Spring boot","This course will help you to understand features of Spring boot","Codeverse",30000,50));
		
		Optional<User> buyer = Optional.of(new User("mahesh","InventoryBuyer","8ef09efc7c34c02e79611e50c722a042759970c4d0fe8e1ec7e9e48501daf788",new Date()));
		Mockito.when(inventoryUserRepository.findById("mahesh")).thenReturn(buyer);
		
		Mockito.when(inventoryProductRepository.findById((long) 301)).thenReturn(product);
		assertThrows(ProductNotFoundException.class, () -> inventoryServiceImpl.buyProduct(106L,"3","8ef09efc7c34c02e79611e50c722a042759970c4d0fe8e1ec7e9e48501daf788"));
	}
	
	
	@Test
	@DisplayName("BuyProduct_InValidUser_NotAuthorizedException")
	void whenInValidUserInBuyProduct_thenReturnNotAuthorizedException() throws NotAuthorizedException, TokenExpiredException, ProductNotFoundException {
		
		Optional<Product> product =Optional.of(new Product(301,"Spring boot","This course will help you to understand features of Spring boot","Codeverse",30000,50));
		
		Optional<User> buyer = Optional.of(new User("mahesh","InventoryBuyer","8ef09efc7c34c02e79611e50c722a042759970c4d0fe8e1ec7e9e48501daf788",new Date()));
		Mockito.when(inventoryUserRepository.findById("mahesh")).thenReturn(buyer);
		
		Mockito.when(inventoryProductRepository.findById((long) 301)).thenReturn(product);
		assertThrows(NotAuthorizedException.class, () -> inventoryServiceImpl.buyProduct(106L,"3","94ac92acb7cf09b4da78d99de9368e9256c625dc974b688532762919807c58bbb"));

	}
	
	
	@Test
	@DisplayName("BuyProduct_InValidQuantity_NotAllowedMessage")
	void whenInValidQauntityInBuyProduct_thenReturnNotAllowedMessage() throws NotAuthorizedException, TokenExpiredException, ProductNotFoundException {
		
		Optional<Product> product =Optional.of(new Product(301,"Spring boot","This course will help you to understand features of Spring boot","Codeverse",30000,50));
		
		Optional<User> buyer = Optional.of(new User("mahesh","InventoryBuyer","8ef09efc7c34c02e79611e50c722a042759970c4d0fe8e1ec7e9e48501daf788",new Date()));
		Mockito.when(inventoryUserRepository.findById("mahesh")).thenReturn(buyer);
		
		Mockito.when(inventoryProductRepository.findById((long) 301)).thenReturn(product);
		ResponseEntity<String> expectedResponseEntity = new ResponseEntity<String>("Not allowed to request more than 3 quantity",HttpStatus.FORBIDDEN) ;
		assertEquals(expectedResponseEntity, inventoryServiceImpl.buyProduct(301L, "6", "8ef09efc7c34c02e79611e50c722a042759970c4d0fe8e1ec7e9e48501daf788"));
	}
	
	@Test
	@DisplayName("BuyProduct_InsufficientQauntity_InSufficientMessage")
	void whenInSufficientQauntityInBuyProduct_thenReturnInSufficientMessage() throws NotAuthorizedException, TokenExpiredException, ProductNotFoundException {
		
		Optional<Product> product =Optional.of(new Product(301,"Spring boot","This course will help you to understand features of Spring boot","Codeverse",30000,2));
		Optional<User> buyer = Optional.of(new User("mahesh","InventoryBuyer","8ef09efc7c34c02e79611e50c722a042759970c4d0fe8e1ec7e9e48501daf788",new Date()));
		Mockito.when(inventoryUserRepository.findById("mahesh")).thenReturn(buyer);
		
		Mockito.when(inventoryProductRepository.findById((long) 301)).thenReturn(product);
		ResponseEntity<String> expectedResponseEntity = new ResponseEntity<String>("No Sufficient Quantity of Product",HttpStatus.INSUFFICIENT_STORAGE) ;
		assertEquals(expectedResponseEntity, inventoryServiceImpl.buyProduct(301L, "3", "8ef09efc7c34c02e79611e50c722a042759970c4d0fe8e1ec7e9e48501daf788"));
	}
	
	@Test
	@DisplayName("filterByBrandName_returnFilteredResult")
	void whenfilterByBrandName_thenReturnFilteredResult(){
		
		List<Product> productList = new ArrayList<>();
		productList.add(new Product(301,"Spring boot","This course will help you to understand features of Spring boot","Codeverse",30000,50));
		productList.add(new Product(302,"Spring MVC","This course will help you to understand features of Spring ","Coding Ninjas",40000,60));
		productList.add(new Product(303,"Spring Data JPA","This course will help you to understand features of Spring ","Coding Ninjas",50000,50));
		productList.add(new Product(304,"Spring security","This course will help you to understand features of Spring security","Codeverse",35000,50));
		productList.add(new Product(305,"Spring","This course will help you to understand features of Spring","Codeverse",10000,50));
		
		List<Product> expectedListOfProducts = new ArrayList<>();
		expectedListOfProducts.add(new Product(302,"Spring MVC","This course will help you to understand features of Spring ","Coding Ninjas",40000,60));
		expectedListOfProducts.add(new Product(303,"Spring Data JPA","This course will help you to understand features of Spring ","Coding Ninjas",50000,50));
		
		
		assertEquals(expectedListOfProducts.toString(),inventoryServiceImpl.filterByBrandName(productList,"Coding Ninjas").toString());
	
	}
	
	@Test
	@DisplayName("filterByModelName_returnFilteredResult")
	void whenfilterByModelName_thenReturnFilteredResult(){
		
		List<Product> productList = new ArrayList<>();
		productList.add(new Product(301,"Spring boot","This course will help you to understand features of Spring boot","Codeverse",30000,50));
		productList.add(new Product(302,"Spring MVC","This course will help you to understand features of Spring ","Coding Ninjas",40000,60));
		productList.add(new Product(303,"Spring Data JPA","This course will help you to understand features of Spring ","Coding Ninjas",50000,50));
		productList.add(new Product(304,"Spring security","This course will help you to understand features of Spring security","Codeverse",35000,50));
		productList.add(new Product(305,"Spring","This course will help you to understand features of Spring","Codeverse",10000,50));
	
		List<Product> expectedListOfProducts = new ArrayList<>();
		expectedListOfProducts.add(new Product(302,"Spring MVC","This course will help you to understand features of Spring ","Coding Ninjas",40000,60));
		expectedListOfProducts.add(new Product(303,"Spring Data JPA","This course will help you to understand features of Spring ","Coding Ninjas",50000,50));
		expectedListOfProducts.add(new Product(305,"Spring","This course will help you to understand features of Spring","Codeverse",10000,50));
		
		assertEquals(expectedListOfProducts.toString(),inventoryServiceImpl.filterByModelName(productList,"This course will help you to understand features of Spring ").toString());
	
	}
	
	
	@Test
	@DisplayName("filterByPrice_returnFilteredResult")
	void whenfilterByPrice_thenReturnFilteredResult(){
		
		List<Product> productList = new ArrayList<>();
		productList.add(new Product(301,"Spring boot","This course will help you to understand features of Spring boot","Codeverse",30000,50));
		productList.add(new Product(302,"Spring MVC","This course will help you to understand features of Spring ","Coding Ninjas",40000,60));
		productList.add(new Product(303,"Spring Data JPA","This course will help you to understand features of Spring ","Coding Ninjas",50000,50));
		productList.add(new Product(304,"Spring security","This course will help you to understand features of Spring security","Codeverse",35000,50));
		productList.add(new Product(305,"Spring","This course will help you to understand features of Spring","Codeverse",10000,50));
		
		List<Product> expectedListOfProducts = new ArrayList<>();
		expectedListOfProducts.add(new Product(301,"Spring boot","This course will help you to understand features of Spring boot","Codeverse",30000,50));
		expectedListOfProducts.add(new Product(304,"Spring security","This course will help you to understand features of Spring security","Codeverse",35000,50));
		expectedListOfProducts.add(new Product(305,"Spring","This course will help you to understand features of Spring","Codeverse",10000,50));
		
		assertEquals(expectedListOfProducts.toString(),inventoryServiceImpl.filterByPrice(productList,"35000").toString());
	
	}
	
	
}
