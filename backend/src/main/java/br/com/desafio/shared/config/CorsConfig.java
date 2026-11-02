package br.com.desafio.shared.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// CORS: diz ao NAVEGADOR quais sites (origens) podem chamar esta API.
// Sem isso, o front em localhost:4200 é bloqueado ao chamar localhost:8080, porque a porta é diferente.
// Atenção: CORS não é segurança contra Postman ou curl; ele só controla o que o navegador permite.
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final String[] origensPermitidas;

    // Lê a lista do application.yml (app.cors.origens-permitidas). O Spring separa as vírgulas sozinho.
    public CorsConfig(@Value("${app.cors.origens-permitidas}") String[] origensPermitidas) {
        this.origensPermitidas = origensPermitidas;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(origensPermitidas)
                // OPTIONS é o "preflight": a pergunta que o navegador faz antes de um POST, PUT, PATCH ou DELETE.
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                // Por padrão o navegador esconde quase todos os headers do front.
                // Expor o Location deixa o Angular ler o endereço do recurso criado (POST → 201).
                .exposedHeaders("Location")
                // O navegador guarda a resposta do preflight por 1 hora, em vez de perguntar a cada chamada.
                .maxAge(3600);
    }
}
