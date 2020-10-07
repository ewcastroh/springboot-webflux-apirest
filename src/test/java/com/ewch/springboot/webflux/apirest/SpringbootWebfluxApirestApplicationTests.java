package com.ewch.springboot.webflux.apirest;

import com.ewch.springboot.webflux.apirest.model.document.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class SpringbootWebfluxApirestApplicationTests {

	@Autowired
	private WebTestClient webTestClient;

	@Test
	public void getAllProductsTest() {
		webTestClient.get()
		.uri("/api/v2/products")
		.accept(MediaType.APPLICATION_JSON_UTF8)
		.exchange()
		.expectStatus().isOk()
		.expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
		.expectBodyList(Product.class)
		.hasSize(9);
	}

}
