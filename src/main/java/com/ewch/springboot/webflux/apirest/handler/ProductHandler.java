package com.ewch.springboot.webflux.apirest.handler;

import com.ewch.springboot.webflux.apirest.model.document.Product;
import com.ewch.springboot.webflux.apirest.service.ProductService;
import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class ProductHandler {

	@Value("${config.uploads.path}")
	private String pathUploads;

	private final ProductService productService;

	public ProductHandler(ProductService productService) {
		this.productService = productService;
	}

	public Mono<ServerResponse> getAllProducts(ServerRequest serverRequest) {
		return ServerResponse.ok()
			.contentType(MediaType.APPLICATION_JSON_UTF8)
			.body(productService.findAll(), Product.class);
	}

	public Mono<ServerResponse> getProductById(ServerRequest serverRequest) {
		String productId = serverRequest.pathVariable("id");
		return productService.findById(productId)
			.flatMap(product -> ServerResponse.ok()
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.body(BodyInserters.fromObject(product))
				.switchIfEmpty(ServerResponse.notFound().build())
			);
	}

	public Mono<ServerResponse> createProduct(ServerRequest serverRequest) {
		Mono<Product> productMono = serverRequest.bodyToMono(Product.class);
		return productMono.flatMap(product -> {
			if (product.getCreatedAt() == null) {
				product.setCreatedAt(new Date());
			}
			return productService.save(product);
		})
			.flatMap(product -> ServerResponse.created(URI.create("/api/v2/products/".concat(product.getId())))
			.contentType(MediaType.APPLICATION_JSON_UTF8)
			.body(BodyInserters.fromObject(product)));
	}

	public Mono<ServerResponse> updateProduct(ServerRequest serverRequest) {
		Mono<Product> productMono = serverRequest.bodyToMono(Product.class);
		String productId = serverRequest.pathVariable("id");
		Mono<Product> productMonoDB = productService.findById(productId);
		return productMonoDB.zipWith(productMono, (productDB, productReq) -> {
			productDB.setName(productReq.getName());
			productDB.setPrice(productReq.getPrice());
			productDB.setCategory(productReq.getCategory());
			return productDB;
		})
			.flatMap(product -> ServerResponse.created(URI.create("/api/v2/products/".concat(product.getId())))
			.contentType(MediaType.APPLICATION_JSON)
			.body(productService.save(product), Product.class))
			.switchIfEmpty(ServerResponse.notFound().build());
	}

	public Mono<ServerResponse> deleteProduct(ServerRequest serverRequest) {
		String productId = serverRequest.pathVariable("id");
		Mono<Product> productMonoDB = productService.findById(productId);
		return productMonoDB.flatMap(product -> {
			return productService.delete(product)
				.then(ServerResponse.noContent().build());
		}).switchIfEmpty(ServerResponse.notFound().build());
	}

	public Mono<ServerResponse> uploadPicture(ServerRequest serverRequest) {
		String productId = serverRequest.pathVariable("id");
		return serverRequest.multipartData()
			.map(stringPartMultiValueMap -> stringPartMultiValueMap.toSingleValueMap().get("file"))
			.cast(FilePart.class)
			.flatMap(filePart -> productService.findById(productId)
				.flatMap(product -> {
					product.setPicture(UUID.randomUUID().toString().concat("-")
						.concat(filePart.filename()
							.replace(" ", "")
							.replace(":", "")
							.replace("\\", "")
						));
					return filePart.transferTo(new File(pathUploads.concat(product.getPicture())))
						.then(productService.save(product));
				})).flatMap(product -> ServerResponse.created(URI.create("/api/v2/products/".concat(product.getId())))
				.contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromObject(product)))
			.switchIfEmpty(ServerResponse.notFound().build());
	}
}
