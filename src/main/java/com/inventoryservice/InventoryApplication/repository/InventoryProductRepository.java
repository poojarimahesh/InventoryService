package com.inventoryservice.InventoryApplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.inventoryservice.InventoryApplication.entity.Product;

@Repository
public interface InventoryProductRepository extends JpaRepository<Product, Long>{
	

}
