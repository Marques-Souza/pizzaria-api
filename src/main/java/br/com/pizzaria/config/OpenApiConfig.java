package br.com.pizzaria.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", securityScheme()))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .info(new Info()
                        .title("Pizzaria API")
                        .description("API para gerenciamento de usuarios, categorias, produtos e pedidos.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Marques Souza")
                                .email("marques.wsouza357@gmail.com")));
    }

    private SecurityScheme securityScheme() {
        return new SecurityScheme()
                .description("JWT Authorization header using the Bearer scheme.")
                .type(SecurityScheme.Type.HTTP)
                .in(SecurityScheme.In.HEADER)
                .scheme("bearer")
                .bearerFormat("JWT");
    }
}
