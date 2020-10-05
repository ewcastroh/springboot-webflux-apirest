package com.ewch.springboot.webflux.apirest.controller;

import com.ewch.springboot.webflux.apirest.model.document.Product;
import com.ewch.springboot.webflux.apirest.service.ProductService;
import java.net.URI;
import java.util.Date;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/products")
public class ProductController {

	private final ProductService productService;

	public ProductController(ProductService productService) {
		this.productService = productService;
	}

	@GetMapping("/flux")
	public Flux<Product> getAllProductsFlux() {
		return productService.findAll();
	}

	@GetMapping("/mono")
	public Mono<ResponseEntity<Flux<Product>>> getAllProductsMono() {
		return Mono.just(ResponseEntity.ok()
			.contentType(MediaType.APPLICATION_JSON_UTF8)
			.body(productService.findAll())
		);
	}

	@GetMapping("/{id}")
	public Mono<ResponseEntity<Product>> getProductById(@PathVariable("id") String id) {
		return productService.findById(id)
			.map(product -> ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.body(product))
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@PostMapping
	public Mono<ResponseEntity<Product>> createProduct(@RequestBody Product product) {
		if (product.getCreatedAt() == null) {
			product.setCreatedAt(new Date());
		}
		return productService.save(product)
			.map(product1 ->
				ResponseEntity.created(URI.create("/api/products/".concat(product.getId())))
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.body(product)
			);
	}

	@PutMapping("/{id}")
	public Mono<ResponseEntity<Product>> updateProduct(@RequestBody Product product, @PathVariable("id") String id) {
		return productService.findById(id)
			.flatMap(updatedProduct -> {
				updatedProduct.setName(product.getName());
				updatedProduct.setPrice(product.getPrice());
				updatedProduct.setCategory(product.getCategory());
				return productService.save(product);
			})
			.map(product1 -> ResponseEntity.created(URI.create("/api/products/".concat(product1.getId())))
			.contentType(MediaType.APPLICATION_JSON_UTF8)
			.body(product1))
			.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	public Mono<ResponseEntity<Void>> deleteProduct(@PathVariable("id") String id) {
		return productService.findById(id)
			.flatMap(product -> productService.delete(product)
				.then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT))))
			.defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
	}
}
