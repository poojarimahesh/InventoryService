package com.inventoryservice.InventoryApplication.entity;

import org.hibernate.validator.constraints.Length;
import org.springframework.stereotype.Component;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Component
@Entity
public class Product {
	
	@Id
	
//	@NotNull(message = "ProductId cannot be null")
	private long productId;
	private String productName;
	private String modelName;
	private String brandName;
	private long price;
//	@Digits(fraction = 0, integer = 100)
//	@Min(value = 1)
//	@NotNull
	private long quantity;
	
	
		
	@Override
	public String toString() {
		return "Product [productId=" + productId + ", productName=" + productName + ", modelName=" + modelName
				+ ", brandName=" + brandName + ", price=" + price + ", quantity=" + quantity + "]";
	}
	public long getProductId() {
		return productId;
	}
	public void setProductId(long productId) {
		this.productId = productId;
	}
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	public String getModelName() {
		return modelName;
	}
	public void setModelName(String modelName) {
		this.modelName = modelName;
	}
	public String getBrandName() {
		return brandName;
	}
	public void setBrandName(String brandName) {
		this.brandName = brandName;
	}
	public long getPrice() {
		return price;
	}
	public void setPrice(long price) {
		this.price = price;
	}
	public long getQuantity() {
		return quantity;
	}
	public Product(long productId, String productName, String modelName, String brandName, long price, long quantity) {
		super();
		this.productId = productId;
		this.productName = productName;
		this.modelName = modelName;
		this.brandName = brandName;
		this.price = price;
		this.quantity = quantity;
	}
	public Product() {
		super();
		// TODO Auto-generated constructor stub
	}
	public void setQuantity(long quantity) {
		this.quantity = quantity;
	}
	
}
