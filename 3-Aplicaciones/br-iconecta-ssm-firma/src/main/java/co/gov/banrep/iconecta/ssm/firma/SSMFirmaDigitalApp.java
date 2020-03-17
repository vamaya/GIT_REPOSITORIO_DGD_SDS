package co.gov.banrep.iconecta.ssm.firma;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

/**
 * Inicia la aplicacion Spring Boot de la Maquina de Estados
 * 
 *  @author <a href="mailto:jjrojassa@banrep.gov.co">John Jairo Rojas S.</a>
 *
 */
@SpringBootApplication
public class SSMFirmaDigitalApp extends SpringBootServletInitializer{
	
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application){
		return application.sources(SSMFirmaDigitalApp.class);
	}
			
	public static void main(String[] args) {
		 SpringApplication.run(SSMFirmaDigitalApp.class, args).close();		
	}
	
}
