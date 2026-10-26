package com.amigoscode.bookstore;

import org.springframework.boot.SpringApplication;

/**
 * Runs the app against a Testcontainers Postgres instead of compose.yaml.
 * Handy when you want a fresh database every time.
 */
public class TestBookstoreApplication {

	public static void main(String[] args) {
		SpringApplication.from(BookstoreApplication::main).with(TestcontainersConfig.class).run(args);
	}

}
