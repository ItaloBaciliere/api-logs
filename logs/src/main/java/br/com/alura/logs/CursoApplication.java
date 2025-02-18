package br.com.alura.logs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CursoApplication {

	private static Logger logger = LoggerFactory.getLogger(CursoApplication.class);

	public static void main(String[] args) {
		logger.info("Iniciando aplicacao!");
		SpringApplication.run(CursoApplication.class, args);
	}
}
