package com.ewch.springboot.webflux.apirest.handler;

import com.ewch.springboot.webflux.apirest.model.document.Category;
import com.ewch.springboot.webflux.apirest.model.document.Product;
import com.ewch.springboot.webflux.apirest.service.ProductService;
import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ProductHandler {

	@Value("${config.uploads.path}")
	private String pathUploads;

	private final ProductService productService;
	private final Validator validator;

	public ProductHandler(ProductService productService, Validator validator) {
		this.productService = productService;
		this.validator = validator;
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
			Errors errors = new BeanPropertyBindingResult(product, Product.class.getName());
			validator.validate(product, errors);

			if (errors.hasErrors()) {
				return Flux.fromIterable(errors.getFieldErrors())
					.map(fieldError -> "Field ".concat(fieldError.getField()).concat(" ").concat(fieldError.getDefaultMessage()))
					.collectList()
					.flatMap(strings -> ServerResponse.badRequest()
						.body(BodyInserters.fromObject(strings)));
			} else {
				if (product.getCreatedAt() == null) {
					product.setCreatedAt(new Date());
				}
				return productService.save(product)
					.flatMap(savedProduct -> ServerResponse.created(URI.create("/api/v2/products/".concat(savedProduct.getId())))
						.contentType(MediaType.APPLICATION_JSON_UTF8)
						.body(BodyInserters.fromObject(savedProduct)));
			}
		});
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

	public Mono<ServerResponse> createProductPicture(ServerRequest serverRequest) {

		Mono<Product> productMono = serverRequest.multipartData()
			.map(stringPartMultiValueMap -> {
				FormFieldPart productName = (FormFieldPart) stringPartMultiValueMap.toSingleValueMap().get("name");
				FormFieldPart productPrice = (FormFieldPart) stringPartMultiValueMap.toSingleValueMap().get("price");
				FormFieldPart productCategoryId = (FormFieldPart) stringPartMultiValueMap.toSingleValueMap().get("category.id");
				FormFieldPart productCategoryName = (FormFieldPart) stringPartMultiValueMap.toSingleValueMap().get("category.name");

				Category category = new Category(productCategoryName.value());
				category.setId(productCategoryId.value());
				return new Product(productName.value(), Double.parseDouble(productPrice.value()), category);
			});

		return serverRequest.multipartData()
			.map(stringPartMultiValueMap -> stringPartMultiValueMap.toSingleValueMap().get("file"))
			.cast(FilePart.class)
			.flatMap(filePart -> productMono
				.flatMap(product -> {
					product.setPicture(UUID.randomUUID().toString().concat("-")
						.concat(filePart.filename()
							.replace(" ", "")
							.replace(":", "")
							.replace("\\", "")
						));
					product.setCreatedAt(new Date());
					return filePart.transferTo(new File(pathUploads.concat(product.getPicture())))
						.then(productService.save(product));
				})).flatMap(product -> ServerResponse.created(URI.create("/api/v2/products/".concat(product.getId())))
				.contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromObject(product)));
	}
}
