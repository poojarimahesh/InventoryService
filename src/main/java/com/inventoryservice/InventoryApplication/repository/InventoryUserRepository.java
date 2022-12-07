package com.inventoryservice.InventoryApplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import com.inventoryservice.InventoryApplication.entity.User;

@Repository
public interface InventoryUserRepository extends JpaRepository<User, String>{
	

}
