package com.all4land.parkinglotnavigator.monitor;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI parkinglotnavigatorOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Parking Lot Navigator BE API")
                        .description("Parking Lot Navigator backend API documentation")
                        .version("v1")
                        .contact(new Contact().name("ALL1Land"))
                        .license(new License().name("Internal Use")));
    }
}
