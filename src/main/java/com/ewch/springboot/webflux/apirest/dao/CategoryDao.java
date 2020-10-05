package com.ewch.springboot.webflux.apirest.dao;

import com.ewch.springboot.webflux.apirest.model.document.Category;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryDao extends ReactiveMongoRepository<Category, String> {

}
