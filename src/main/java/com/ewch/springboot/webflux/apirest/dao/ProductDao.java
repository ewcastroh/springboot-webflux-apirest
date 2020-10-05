package com.ewch.springboot.webflux.apirest.dao;

import com.ewch.springboot.webflux.apirest.model.document.Product;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductDao extends ReactiveMongoRepository<Product, String> {

}
