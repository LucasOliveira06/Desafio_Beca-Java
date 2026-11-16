package br.com.lucas.ms2_transaction.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI ms2TransactionOpenApi() {
        String securitySchemeName = "basicAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("MS2 Transaction")
                        .description(
                                "API de transações e gestão financeira"
                        )
                        .version("1.0")
                )
                .components(new Components()
                        .addSecuritySchemes(
                                securitySchemeName,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("basic")
                        )
                )
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList(securitySchemeName)
                );
    }
}