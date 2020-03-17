package co.gov.banrep.iconecta.pdf.app_utils;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

import co.gov.banrep.iconecta.cs.autenticacion.AutenticacionCS;




@SpringBootApplication
public class BrPdfAppUtilsAplication  extends SpringBootServletInitializer implements CommandLineRunner {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(BrPdfAppUtilsAplication.class);
	}
	
	@Autowired
	private AutenticacionCS autenticacionCS;
	
	public static void main(String[] args) {
		SpringApplication.run(BrPdfAppUtilsAplication.class, args);
	}
	
	@Override
	public void run(String... arg0) throws Exception {
		try {
			autenticacionCS.authenticate();
			log.info("Autenticacion Aplicacion - Content Server exitosa");
		} catch (IOException e) {
			log.error("Error autenticacion con Content Server");
			log.error("Revise los datos de las propiedades content dentro de los archivos de properties", e);
			throw new RuntimeException();
		}
		
	}
	
}
