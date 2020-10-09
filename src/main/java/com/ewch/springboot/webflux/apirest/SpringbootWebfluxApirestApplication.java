package com.ewch.springboot.webflux.apirest;

import com.ewch.springboot.webflux.apirest.model.document.Category;
import com.ewch.springboot.webflux.apirest.model.document.Product;
import com.ewch.springboot.webflux.apirest.service.ProductService;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Flux;

@EnableEurekaClient
@SpringBootApplication
public class SpringbootWebfluxApirestApplication implements CommandLineRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(SpringbootWebfluxApirestApplication.class);

	private final ProductService productService;

	private final ReactiveMongoTemplate reactiveMongoTemplate;

	public SpringbootWebfluxApirestApplication(ProductService productService, ReactiveMongoTemplate reactiveMongoTemplate) {
		this.productService = productService;
		this.reactiveMongoTemplate = reactiveMongoTemplate;
	}

	public static void main(String[] args) {
		SpringApplication.run(SpringbootWebfluxApirestApplication.class, args);
	}


	@Override
	public void run(String... args) throws Exception {

		reactiveMongoTemplate.dropCollection("products").subscribe();
		reactiveMongoTemplate.dropCollection("categories").subscribe();

		Category electronic = new Category("Electronic");
		Category sport = new Category("Sport");
		Category computation = new Category("Computation");
		Category furniture = new Category("Furniture");

		Flux.just(electronic, sport, computation, furniture)
			.flatMap(productService::saveCategory)
			.doOnNext(category -> LOGGER.info("Category saved :: ".concat(category.getName())))
			.thenMany(Flux.just(
				new Product("TV Panasonic LCD", 456.89, electronic),
				new Product("Sony Camara HD Digital", 177.89, electronic),
				new Product("Apple iPod", 46.89, electronic),
				new Product("Sony Notebook", 846.89, computation),
				new Product("Hewlett Packard Multifuncional", 200.89, computation),
				new Product("Bianchi Bicycle", 70.89, sport),
				new Product("HP Notebook Omen 17", 2500.89, computation),
				new Product("Mica 5 Drawers", 150.89, furniture),
				new Product("TV Sony Bravia OLED 4K Ultra HD", 2255.89, electronic))
				.flatMap(product -> {
					product.setCreatedAt(new Date());
					return productService.save(product);
				}))
			.subscribe(
				productMono -> LOGGER.info("Insert :: ".concat(productMono.getId()).concat(": ").concat(productMono.getName()))
			);
	}

}
