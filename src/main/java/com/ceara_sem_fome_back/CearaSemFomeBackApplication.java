package com.ceara_sem_fome_back;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@SpringBootApplication
public class CearaSemFomeBackApplication {
    public static void main(String[] args) {
        log.info("Ceará sem Fome");
        SpringApplication.run(CearaSemFomeBackApplication.class, args);
    }
}