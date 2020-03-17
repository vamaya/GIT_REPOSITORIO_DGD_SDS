package co.gov.banrep.iconecta.cs.app.service.utils;

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

/**
 * Inicia la aplicacion Service Utils y realiza la autenticacion contra
 * Content Server de Opentext
 * 
 *  @author <a href="mailto:jjrojassa@banrep.gov.co">John Jairo Rojas S.</a>
 *
 */
@SpringBootApplication
public class CsServiceUtilsApplication extends SpringBootServletInitializer implements CommandLineRunner {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Override
	protected SpringApplicationBuilder configure (SpringApplicationBuilder application){
		return application.sources(CsServiceUtilsApplication.class);
	}

	@Autowired
	private AutenticacionCS autenticacionCS;

	public static void main(String[] args) {
		SpringApplication.run(CsServiceUtilsApplication.class, args);
	}

	@Override
	public void run(String... arg0) throws Exception {
		log.info("INICIANDO APLICACION");

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
