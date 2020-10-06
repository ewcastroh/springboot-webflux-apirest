package com.ewch.springboot.webflux.apirest.handler;

import com.ewch.springboot.webflux.apirest.model.document.Product;
import com.ewch.springboot.webflux.apirest.service.ProductService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class ProductHandler {

	private final ProductService productService;

	public ProductHandler(ProductService productService) {
		this.productService = productService;
	}

	public Mono<ServerResponse> getAllProducts(ServerRequest serverRequest) {
		return ServerResponse.ok()
			.contentType(MediaType.APPLICATION_JSON_UTF8)
			.body(productService.findAll(), Product.class);
	}
}
