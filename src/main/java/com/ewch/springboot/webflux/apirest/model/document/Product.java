package com.ewch.springboot.webflux.apirest.model.document;

import java.util.Date;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

@Document(collection = "products")
public class Product {

	@Id
	private String id;

	@NotEmpty
	private String name;

	@NotNull
	private Double price;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date createdAt;

	@Valid
	@NotNull
	private Category category;

	private String picture;

	public Product() {
	}

	public Product(String name, Double price) {
		this.name = name;
		this.price = price;
	}

	public Product(String name, Double price, Category category) {
		this(name, price);
		this.category = category;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public String getPicture() {
		return picture;
	}

	public void setPicture(String picture) {
		this.picture = picture;
	}

	@Override
	public String toString() {
		return "Product{" +
			"id='" + id + '\'' +
			", name='" + name + '\'' +
			", price=" + price +
			", createdAt=" + createdAt +
			", category=" + category +
			'}';
	}
}
