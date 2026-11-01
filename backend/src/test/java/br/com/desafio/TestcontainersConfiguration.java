package br.com.desafio;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

// Configuração usada só nos testes: a aplicação real não sobe este container.
@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    // @ServiceConnection passa a URL, o usuário e a senha do container para o Spring,
    // sem precisar configurar a conexão de teste em nenhum arquivo.
    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        // Mesma imagem do docker-compose.yml: os testes rodam no mesmo banco da aplicação.
        return new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"));
    }

}
