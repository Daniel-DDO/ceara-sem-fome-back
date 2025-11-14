package com.ceara_sem_fome_back.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Ceará Sem Fome - API")
                        .version("v1")
                        .description("API para o projeto Ceará Sem Fome")
                        .contact(new Contact().name("Equipe Ceará Sem Fome").email("contato@cearasemfome.org"))
                        .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")));
    }
}
