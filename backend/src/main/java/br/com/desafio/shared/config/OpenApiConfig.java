package br.com.desafio.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Cabeçalho da documentação: é o que aparece no topo do Swagger UI.
// As rotas em si o springdoc descobre sozinho, lendo os controllers e os DTOs.
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI documentacaoDaApi() {
        return new OpenAPI().info(new Info()
                .title("API de Usuários e Cartões de Ônibus")
                .description("Cadastro de usuários e dos seus cartões de ônibus (COMUM, ESTUDANTE e TRABALHADOR). "
                        + "Todos os erros seguem o formato { timestamp, status, erro, mensagem, caminho }.")
                .version("v1"));
    }
}
