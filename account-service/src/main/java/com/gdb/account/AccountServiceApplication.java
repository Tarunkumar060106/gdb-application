package com.gdb.account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * Main entry point for the Account Microservice.
 * This service manages bank accounts, balances, and integrations.
 */
@SpringBootApplication
public class AccountServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountServiceApplication.class, args);
    }

    // TODO: MOD1-CR-01: Decouple RestTemplate Bean configuration.
    // The RestTemplate bean is currently declared inside this main Boot class.
    // Task: Extract this configuration to a dedicated configuration class `RestTemplateConfig` 
    // inside the `com.gdb.account.config` package, and annotate it with @Configuration.
    // Customize the RestTemplate using SimpleClientHttpRequestFactory to add request/connect timeouts.

    // Added a separate configuration class -> RestTemplateConfig
    // created a SimpleClientHttpRequestFactory factory and added request and connect timeouts of 5 seconds
}
