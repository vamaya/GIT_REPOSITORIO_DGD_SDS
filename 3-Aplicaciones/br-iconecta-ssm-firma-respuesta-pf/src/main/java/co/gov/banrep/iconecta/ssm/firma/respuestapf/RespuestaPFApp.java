package co.gov.banrep.iconecta.ssm.firma.respuestapf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

/**
 * Inicia la aplicacion de Respuesta de Portafirmas
 * 
 *  @author <a href="mailto:jjrojassa@banrep.gov.co">John Jairo Rojas S.</a>
 *
 */
@SpringBootApplication
//@EnableAsync
public class RespuestaPFApp extends SpringBootServletInitializer{
	
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application){
		return application.sources(RespuestaPFApp.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(RespuestaPFApp.class, args).close();
	}
	
	/*
	@Bean
	public Executor asyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(2);
		executor.setMaxPoolSize(2);
		executor.setQueueCapacity(100);
		executor.setThreadNamePrefix("RespuestPFAsync-");
		executor.initialize();
		
		return executor;
	}
	*/
	
}
