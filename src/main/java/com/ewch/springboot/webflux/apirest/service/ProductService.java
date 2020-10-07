package com.ewch.springboot.webflux.apirest.service;

import com.ewch.springboot.webflux.apirest.model.document.Category;
import com.ewch.springboot.webflux.apirest.model.document.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductService {

	Flux<Product> findAll();

	Flux<Product> findAllWithUpperCaseName();

	Flux<Product> findAllWithUpperCaseNameRepeat();

	Mono<Product> findById(String id);

	Mono<Product> findByName(String name);

	Mono<Product> findByNameQuery(String name);

	Mono<Product> save(Product product);

	Mono<Void> delete(Product product);

	Flux<Category> findAllCategories();

	Mono<Category> findCategoryById(String id);

	Mono<Category> findCategoryByName(String name);

	Mono<Category> saveCategory(Category category);
}
