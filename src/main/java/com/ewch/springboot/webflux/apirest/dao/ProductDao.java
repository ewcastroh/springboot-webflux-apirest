package com.ewch.springboot.webflux.apirest.dao;

import com.ewch.springboot.webflux.apirest.model.document.Product;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ProductDao extends ReactiveMongoRepository<Product, String> {

	Mono<Product> findProductByName(String name);

	@Query("{ 'name': ?0' }")
	Mono<Product> findProductByNameQuery(String name);
}
