package br.com.gestaocompras.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Configuracao {

    /** Relógio injetável: nos testes dá para fixar a data e hora. */
    @Bean
    public Clock relogio() {
        return Clock.systemDefaultZone();
    }
}
