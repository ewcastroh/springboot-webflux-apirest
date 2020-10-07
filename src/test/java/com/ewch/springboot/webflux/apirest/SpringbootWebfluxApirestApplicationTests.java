package com.ewch.springboot.webflux.apirest;

import com.ewch.springboot.webflux.apirest.model.document.Category;
import com.ewch.springboot.webflux.apirest.model.document.Product;
import com.ewch.springboot.webflux.apirest.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class SpringbootWebfluxApirestApplicationTests {

	@Value("${config.base.endpoint}")
	private String urlEndpoint;

	@Autowired
	private WebTestClient webTestClient;

	@Autowired
	private ProductService productService;

	@Test
	public void getAllProductsTest() {
		webTestClient.get()
		.uri(urlEndpoint)
		.accept(MediaType.APPLICATION_JSON_UTF8)
		.exchange()
		.expectStatus().isOk()
		.expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
		.expectBodyList(Product.class)
		//.hasSize(9);
		.consumeWith(listEntityExchangeResult -> {
			List<Product> productList = listEntityExchangeResult.getResponseBody();
			productList.forEach(product -> {
				System.out.println(product.getName());
			});
			Assertions.assertThat(productList.size() > 0).isTrue();
		});
	}

	@Test
	public void getProductByIdTest() {
		Mono<Product> productMono = productService.findByName("TV Panasonic LCD");

		webTestClient.get()
		.uri(urlEndpoint.concat("/{id}"), Collections.singletonMap("id", productMono.block().getId()))
		.accept(MediaType.APPLICATION_JSON_UTF8)
		.exchange()
		.expectStatus().isOk()
		.expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
		/*.expectBody()
		.jsonPath("$.id").isNotEmpty()
		.jsonPath("$.name").isEqualTo("TV Panasonic LCD");*/
		.expectBody(Product.class)
		.consumeWith(productEntityExchangeResult -> {
			Product product = productEntityExchangeResult.getResponseBody();
			Assertions.assertThat(product.getId()).isNotEmpty();
			Assertions.assertThat(product.getId().length() > 0).isTrue();
			Assertions.assertThat(product.getName()).isEqualTo("TV Panasonic LCD");
		});
	}

	@Test
	public void createProductTest() {
		Mono<Category> categoryMono = productService.findCategoryByName("Furniture");

		Product product = new Product("Dinner Table", 357.78, categoryMono.block());
		webTestClient.post()
			.uri(urlEndpoint)
			.contentType(MediaType.APPLICATION_JSON_UTF8)
			.accept(MediaType.APPLICATION_JSON_UTF8)
			.body(Mono.just(product), Product.class)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
			.expectBody()
			// .jsonPath("$.id").isNotEmpty()
			// .jsonPath("$.name").isEqualTo("Dinner Table")
			// .jsonPath("$.category.name").isEqualTo("Furniture");
			.jsonPath("$.product.id").isNotEmpty()
			.jsonPath("$.product.name").isEqualTo("Dinner Table")
			.jsonPath("$.product.category.name").isEqualTo("Furniture");
	}

	@Test
	public void createProductV2Test() {
		Mono<Category> categoryMono = productService.findCategoryByName("Furniture");

		Product newProduct = new Product("Dinner Table", 357.78, categoryMono.block());
		webTestClient.post()
			.uri(urlEndpoint)
			.contentType(MediaType.APPLICATION_JSON_UTF8)
			.accept(MediaType.APPLICATION_JSON_UTF8)
			.body(Mono.just(newProduct), Product.class)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
			// .expectBody(Product.class)
			.expectBody(new ParameterizedTypeReference<LinkedHashMap<String, Object>>() {})
			.consumeWith(productEntityExchangeResult -> {
				// Product product = productEntityExchangeResult.getResponseBody();
				Object o = productEntityExchangeResult.getResponseBody().get("product");
				Product product = new ObjectMapper().convertValue(o, Product.class);
				Assertions.assertThat(product.getId()).isNotEmpty();
				Assertions.assertThat(product.getName()).isEqualTo("Dinner Table");
				Assertions.assertThat(product.getCategory().getName()).isEqualTo("Furniture");
			});
	}

	@Test
	public void updateProductTest() {
		Mono<Product> productMono = productService.findByName("Sony Notebook");
		Mono<Category> categoryMono = productService.findCategoryByName("Electronic");

		Product editedProduct = new Product("Asus Notebook", 100.21, categoryMono.block());
		webTestClient.put()
			.uri(urlEndpoint.concat("/{id}"), Collections.singletonMap("id", productMono.block().getId()))
			.contentType(MediaType.APPLICATION_JSON_UTF8)
			.accept(MediaType.APPLICATION_JSON_UTF8)
			.body(Mono.just(editedProduct), Product.class)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
			.expectBody()
			.jsonPath("$.id").isNotEmpty()
			.jsonPath("$.name").isEqualTo("Asus Notebook")
			.jsonPath("$.category.name").isEqualTo("Electronic");
	}

	@Test
	public void deleteProduct() {
		Mono<Product> productMono = productService.findByName("Mica 5 Drawers");
		webTestClient.delete()
			.uri(urlEndpoint.concat("/{id}"), Collections.singletonMap("id", productMono.block().getId()))
			.exchange()
			.expectStatus().isNoContent()
			.expectBody().isEmpty();

		webTestClient.get()
			.uri(urlEndpoint.concat("/{id}"), Collections.singletonMap("id", productMono.block().getId()))
			.exchange()
			.expectStatus().isNotFound()
			.expectBody().isEmpty();
	}
}
