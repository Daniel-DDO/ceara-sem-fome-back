package com.ceara_sem_fome_back;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@EnableScheduling
@SpringBootApplication
public class CearaSemFomeBackApplication {
    public static void main(String[] args) {
        log.info("Ceará sem Fome");
        SpringApplication.run(CearaSemFomeBackApplication.class, args);
    }
}