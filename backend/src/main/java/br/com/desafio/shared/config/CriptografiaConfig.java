
package br.com.desafio.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// Só temos o spring-security-crypto (sem o starter de segurança), então ninguém cria este bean sozinho.
// Como bean, o algoritmo é trocado num lugar só, e nos testes dá para usar um mock no lugar.
@Configuration
public class CriptografiaConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
