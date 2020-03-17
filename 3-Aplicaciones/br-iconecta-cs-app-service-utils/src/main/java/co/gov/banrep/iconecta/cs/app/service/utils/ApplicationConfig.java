package co.gov.banrep.iconecta.cs.app.service.utils;

import java.io.IOException;

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import co.gov.banrep.iconecta.cs.autenticacion.AutenticacionCS;
import co.gov.banrep.iconecta.cs.documento.Nodo;

/**
 * Configuracion de la aplicacion Service Utils
 * 
 * @author desa_jrojassa
 *
 */
@Configuration
//@PropertySource("file:${ICONECTAFILES_PATH}/service_utils.properties")
@PropertySource("file:/apps/iconectaFiles_16/service_utils.properties")
@ConfigurationProperties("serviceutils")
public class ApplicationConfig {
	@NotNull
	private String username;		
	@NotNull
	private String cuenta;
	@NotNull
	private String endpoint;
	@NotNull
	private String tipo;
	@NotNull
	private String authenticationWsdl;
	@NotNull
	private String documentoWsdl;
	@NotNull
	private String paCon;
	@NotNull
	private String vector;
	
	@Bean
	public Nodo getNodo() throws IOException, InterruptedException {
		return new Nodo(documentoWsdl);
	}
		

	public void setUsername(String username) {
		this.username = username;
	}

	public void setCuenta(String cuenta) {
		this.cuenta = cuenta;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	
	public String getUsername() {
		return username;
	}
	public void setAuthenticationWsdl(String authenticationWsdl) {
		this.authenticationWsdl = authenticationWsdl;
	}
	public void setDocumentoWsdl(String documentoWSDL) {
		this.documentoWsdl = documentoWSDL;
	}
	public void setPaCon(String paCon) {
		this.paCon = paCon;
	}

	public void setVector(String vector) {
		this.vector = vector;
	}

	public String getDocumentoWsdl() {
		return documentoWsdl;
	}

	@Bean
	public AutenticacionCS autenticacionCS() throws Exception{
		AutenticacionCS auth = new AutenticacionCS();
		
		auth.setUsuarioAdmin(username);
		auth.setUrlWSDL(authenticationWsdl);
		
		try {
			auth.setPassword(Encrypt.decrypt(vector, paCon));
		} catch (Exception e) {
			System.err.println("Error obteniedo contraseña");
			e.printStackTrace();
			return null;
		}
				
		return auth;
	}

	
	
}
