package com.ewch.springboot.webflux.apirest.config;

import com.ewch.springboot.webflux.apirest.handler.ProductHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class RouterFunctionConfig {

	@Bean
	public RouterFunction<ServerResponse> routes(ProductHandler productHandler) {
		return RouterFunctions.route(
			RequestPredicates.GET("/api/v2/products").or(RequestPredicates.GET("/api/v3/products")), productHandler::getAllProducts)
			.andRoute(RequestPredicates.GET("/api/v2/products/{id}"), productHandler::getProductById);
	}
}
