package com.ewch.springboot.webflux.apirest.service;

import com.ewch.springboot.webflux.apirest.dao.CategoryDao;
import com.ewch.springboot.webflux.apirest.dao.ProductDao;
import com.ewch.springboot.webflux.apirest.model.document.Category;
import com.ewch.springboot.webflux.apirest.model.document.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductServiceImpl implements ProductService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProductServiceImpl.class);

	private final ProductDao productDao;
	private final CategoryDao categoryDao;

	public ProductServiceImpl(ProductDao productDao, CategoryDao categoryDao) {
		this.productDao = productDao;
		this.categoryDao = categoryDao;
	}

	@Override
	public Flux<Product> findAll() {
		return productDao.findAll();
	}

	@Override
	public Flux<Product> findAllWithUpperCaseName() {
		return productDao.findAll()
			.map(product -> {
				product.setName(product.getName().toUpperCase());
				return product;
		});
	}

	@Override
	public Flux<Product> findAllWithUpperCaseNameRepeat() {
		return findAllWithUpperCaseName().repeat(5000);
	}

	@Override
	public Mono<Product> findById(String id) {
		return productDao.findById(id);
	}

	@Override
	public Mono<Product> save(Product product) {
		return productDao.save(product);
	}

	@Override
	public Mono<Void> delete(Product product) {
		return productDao.delete(product);
	}

	@Override
	public Flux<Category> findAllCategories() {
		return categoryDao.findAll();
	}

	@Override
	public Mono<Category> findCategoryById(String id) {
		return categoryDao.findById(id);
	}

	@Override
	public Mono<Category> saveCategory(Category category) {
		return categoryDao.save(category);
	}
}
