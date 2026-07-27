package com.Projeto.GeradorDeQuestoes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class GeradorDeQuestoesApplication {

	public static void main(String[] args) {
		SpringApplication.run(GeradorDeQuestoesApplication.class, args);
	}

}
